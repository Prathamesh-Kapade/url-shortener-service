package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ShortUrl;
import com.demo.urlshorterservice.repository.ShortUrlRepository;
import com.demo.urlshorterservice.util.ShortCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import com.demo.urlshorterservice.exception.ShortUrlNotFoundException;

import java.time.Duration;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_COLLISION_RETRIES = 10;
    private static final String CACHE_PREFIX = "short:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);


    public UrlShortenerService(ShortUrlRepository shortUrlRepository,
                               ShortCodeGenerator shortCodeGenerator,
                               RedisTemplate<String, String> redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.redisTemplate = redisTemplate;
    }

    public ShortUrl createShortUrl(String originalUrl) {

        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        String shortCode = generateUniqueShortCode();

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);

        return shortUrlRepository.save(shortUrl);
    }


    private String generateUniqueShortCode() {
        int attempts = 0;
        String shortCode;

        do {
            shortCode = shortCodeGenerator.generate();
            attempts++;

            if (attempts > MAX_COLLISION_RETRIES) {
                throw new RuntimeException("Failed to generate unique short code after retries");
            }
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());

        return shortCode;
    }


    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        String cacheKey = CACHE_PREFIX + shortCode;

        // 1. Redis check
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for shortCode={}",shortCode);
            return cached;
        }

        // 2. DB Check — throws if not found
        String originalUrl = shortUrlRepository
                .findByShortCode(shortCode)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(() -> {
                  log.warn("ShortCode not found: {}",shortCode);
                 return new ShortUrlNotFoundException(shortCode);
                });


        redisTemplate.opsForValue().set(cacheKey, originalUrl, CACHE_TTL);

        return originalUrl;

    }
}

