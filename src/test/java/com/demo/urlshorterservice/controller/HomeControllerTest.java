package com.demo.urlshorterservice.controller;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.demo.urlshorterservice.exception.ShortUrlNotFoundException;
import com.demo.urlshorterservice.service.AnalyticsService;
import com.demo.urlshorterservice.service.ClickEventProducer;
import com.demo.urlshorterservice.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlShortenerService urlShortenerService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private ClickEventProducer clickEventProducer;

    @Test
    void should_redirect_when_validShortCode() throws Exception {

        when(urlShortenerService.getOriginalUrl("abc123"))
                .thenReturn("https://google.com");

        ClickEvent mockEvent = new ClickEvent();
        when(analyticsService.buildClickEvent(eq("abc123"), any()))
                .thenReturn(mockEvent);

        mockMvc.perform(get("/r/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://google.com"));

        verify(clickEventProducer).publish(mockEvent);
    }

    @Test
    void should_return404_when_shortCodeNotFound() throws Exception {

        when(urlShortenerService.getOriginalUrl("unknown"))
                .thenThrow(new ShortUrlNotFoundException("unknown"));

        mockMvc.perform(get("/r/unknown"))
                .andExpect(status().isNotFound());

        verify(clickEventProducer, never()).publish(any());
    }

    @Test
    void should_callAnalytics_when_redirectSucceeds() throws Exception {

        when(urlShortenerService.getOriginalUrl("abc123"))
                .thenReturn("https://github.com");

        when(analyticsService.buildClickEvent(eq("abc123"), any()))
                .thenReturn(new ClickEvent());

        mockMvc.perform(get("/r/abc123"))
                .andExpect(status().is3xxRedirection());

        verify(analyticsService).buildClickEvent(eq("abc123"), any());
        verify(clickEventProducer).publish(any());
    }
}