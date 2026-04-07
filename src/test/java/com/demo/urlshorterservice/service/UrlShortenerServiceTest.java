package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ShortUrl;
import com.demo.urlshorterservice.exception.ShortUrlNotFoundException;
import com.demo.urlshorterservice.repository.ShortUrlRepository;
import com.demo.urlshorterservice.util.ShortCodeGenerator;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {

    @Mock
    ShortUrlRepository shortUrlRepository;

    @Mock
    ShortCodeGenerator shortCodeGenerator;

    @Mock
    RedisTemplate<String,String> redisTemplate;

    @Mock
    ValueOperations<String,String> valueOps;  // Redis sub-interface

    @InjectMocks
    UrlShortenerService urlShortenerService; // real class under test

    @BeforeEach
    void setUp(){
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // TEST 1: getOriginalUrl — Cache HIT
    @Test
    void should_returnUrl_when_redisCasheHit(){
        String shortCode = "abc123";
        String cachedUrl = "https://google.com";
        when(valueOps.get("short:abc123")).thenReturn(cachedUrl);

        // Act
        String result = urlShortenerService.getOriginalUrl(shortCode);

        // Assert
        assertEquals(cachedUrl,result);
        verify(shortUrlRepository, never()).findByShortCode(any());
    }

    // TEST 2: getOriginalUrl — Cache MISS, DB HIT
    @Test
    void should_returnUrlFromDb_when_redisCacheMiss(){
        // Arrange - Redis returns null (cache miss)
        when(valueOps.get("short:abc123")).thenReturn(null);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("abc123");
        shortUrl.setOriginalUrl("https://github.com");
        when(shortUrlRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(shortUrl));


        // Act
        String result = urlShortenerService.getOriginalUrl("abc123");

        // Assert
        assertEquals("https://github.com",result);
        // Cache must be populated after DB hit
        verify(valueOps).set(eq("short:abc123"),
                eq("https://github.com"),any());
    }

    // TEST 3: getOriginalUrl — Cache MISS, DB MISS → exception
    @Test
    void should_throwException_when_shortCodeNotFound(){
        // Arrange - both Redis and DB return nothing
        when(valueOps.get(any())).thenReturn(null);
        when(shortUrlRepository.findByShortCode("xyz999"))
                .thenReturn(Optional.empty());

        // Act+Assert- assertThrows checks exception is thrown
        ShortUrlNotFoundException ex =  assertThrows(
                ShortUrlNotFoundException.class,
                () -> urlShortenerService.getOriginalUrl("xyz999")
        );
        assertEquals("xyz999",ex.getShortCode());
    }

    // TEST 4: createShortUrl — new URL
    @Test
    void should_createdAndSaveShortUrl_when_urlIsNew(){
        // Arrange
        String originalUrl = "https://openai.com";
        when(shortUrlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.empty());
        when(shortCodeGenerator.generate()).thenReturn("newcode");
        when(shortUrlRepository.findByShortCode("newcode"))
                .thenReturn(Optional.empty());  // no collision

        ShortUrl saved = new ShortUrl();
        saved.setShortCode("newcode");
        saved.setOriginalUrl(originalUrl);
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenReturn(saved);


        // Act
        ShortUrl result = urlShortenerService.createShortUrl(originalUrl);

        // Assert
        assertEquals("newcode", result.getShortCode());
        verify(shortUrlRepository).save(any(ShortUrl.class));
        // Cache pre-warmed after creation
        verify(valueOps).set(eq("short:newcode"),
                            eq(originalUrl),any());
    }

    // TEST 5: createShortUrl — URL already exists
    @Test
    void should_returnExisting_when_urlAlreadyShortened(){
        // Arrange
        ShortUrl existing = new ShortUrl();
        existing.setShortCode("exist1");
        existing.setOriginalUrl("https://openai.com");
        when(shortUrlRepository.findByOriginalUrl("https://openai.com"))
                .thenReturn(Optional.of(existing));

        // Act
        ShortUrl result = urlShortenerService
                .createShortUrl("https://openai.com");

        // Assert — no new code generated, no DB save
        assertEquals("exist1", result.getShortCode());
        verify(shortCodeGenerator, never()).generate();
        verify(shortUrlRepository, never()).save(any());
    }

    // TEST 6: collision retry — code already taken
    @Test
    void should_retryCodeGeneration_when_collisionOccurs(){
        //Arrange - first code collides, second is unique
        when(shortUrlRepository.findByOriginalUrl(any()))
                .thenReturn(Optional.empty());
        when(shortCodeGenerator.generate())
                .thenReturn("taken1","unique2");

        ShortUrl taken = new ShortUrl();
        taken.setShortCode("taken1");
        when(shortUrlRepository.findByShortCode("taken1"))
                .thenReturn(Optional.of(taken));
        when(shortUrlRepository.findByShortCode("unique2"))
                .thenReturn(Optional.empty());
        when(shortUrlRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        ShortUrl result = urlShortenerService
                .createShortUrl("https://example.com");

        // Assert — generator called twice due to collision
        assertEquals("unique2", result.getShortCode());
        verify(shortCodeGenerator, times(2)).generate();

    }
}
