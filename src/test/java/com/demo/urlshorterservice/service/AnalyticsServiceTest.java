package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.demo.urlshorterservice.model.GeoData;
import com.demo.urlshorterservice.repository.ClickEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    @Mock ClickEventRepository clickEventRepo;
    @Mock GeoIpService         geoIpService;
    @Mock HttpServletRequest   request;         // mock HTTP request

    @InjectMocks
    AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        // Stub geoIpService for ALL tests — prevents null geo in buildClickEvent()
        when(geoIpService.lookup(any()))
                .thenReturn(GeoData.unknown());   // returns GeoData("Unknown","Unknown")
    }


    // ── TEST 1: builds ClickEvent from request ───────────────────
    @Test
    void should_buildClickEvent_with_correctDevice_when_mobileUserAgent() {
        // Arrange
        when(request.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 (Android; Mobile) AppleWebKit");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");
        when(request.getHeader("Referer")).thenReturn("https://twitter.com");

        // Act
        ClickEvent event = analyticsService
                .buildClickEvent("abc123", request);

        // Assert
        assertEquals("abc123",           event.getShortCode());
        assertEquals("Mobile",           event.getDevice());
        assertEquals("203.0.113.0",      event.getIpAddress()); // masked
        assertEquals("https://twitter.com", event.getReferer());
    }

    // ── TEST 2: Desktop detection ────────────────────────────────
    @Test
    void should_detectDesktop_when_normalUserAgent() {
        when(request.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader("Referer")).thenReturn(null);

        ClickEvent event = analyticsService
                .buildClickEvent("xyz", request);

        assertEquals("Desktop", event.getDevice());
        assertEquals("Windows", event.getOs());
        assertEquals("Chrome",  event.getBrowser());
    }

    // ── TEST 3: X-Forwarded-For extraction ──────────────────────
    @Test
    void should_extractFirstIp_when_xForwardedForHasMultiple() {
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("203.0.113.42, 10.0.0.1, 192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getHeader("Referer")).thenReturn(null);

        ClickEvent event = analyticsService
                .buildClickEvent("abc", request);

        // should take first IP and mask it
        assertEquals("203.0.113.0", event.getIpAddress());
    }

    // ── TEST 4: IP masking ──
    @Test
    void should_maskLastOctet_when_buildingClickEvent() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.100.55");
        when(request.getHeader("User-Agent")).thenReturn("Firefox");
        when(request.getHeader("Referer")).thenReturn(null);

        ClickEvent event = analyticsService
                .buildClickEvent("test", request);

        assertEquals("192.168.100.0", event.getIpAddress());
    }
}
