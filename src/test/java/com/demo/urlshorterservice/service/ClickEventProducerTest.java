package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClickEventProducerTest {

    @Mock
    KafkaTemplate<String, byte[]> kafkaTemplate;
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    ClickEventProducer clickEventProducer;

    // ── TEST 1: publishes serialized event to correct topic ──────
    @Test
    void should_publishToKafka_when_validClickEvent() throws Exception {
        // Arrange
        ClickEvent event = new ClickEvent(
                null, "abc123", null,
                "203.0.113.0", null, null,
                "Mobile", "Chrome", "Android", null
        );
        byte[] serialized = "{\"shortCode\":\"abc123\"}".getBytes();
        when(objectMapper.writeValueAsBytes(event)).thenReturn(serialized);

        var future = mock(
                org.springframework.kafka.support.SendResult.class);
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenReturn(mock(
                        java.util.concurrent.CompletableFuture.class));

        // Act
        clickEventProducer.publish(event);

        // Assert — kafka send called with correct topic and key
        verify(kafkaTemplate).send(
                eq("click-events"),
                eq("abc123"),
                eq(serialized)
        );
    }

    // ── TEST 2: does not throw if serialization fails ────────────
    @Test
    void should_notThrow_when_serializationFails() throws Exception {
        // Arrange — ObjectMapper throws
        ClickEvent event = new ClickEvent(
                null, "abc123", null, null,
                null, null, null, null, null, null
        );
        when(objectMapper.writeValueAsBytes(any()))
                .thenThrow(new com.fasterxml.jackson.core
                        .JsonProcessingException("fail") {
                });

        // Act + Assert — must swallow the error gracefully
        assertDoesNotThrow(() -> clickEventProducer.publish(event));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}