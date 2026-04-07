package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClickEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(ClickEventProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    public ClickEventProducer(KafkaTemplate<String, byte[]> kafkaTemplate,
                              ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    public void publish(ClickEvent event) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled — skipping analytics publish");
            return;
        }
        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);
            kafkaTemplate.send("click-events",
                            event.getShortCode(), payload)
                    .whenComplete((r, ex) -> {
                        if (ex != null)
                            log.error("Kafka publish failed: {}", ex.getMessage());
                    });
        } catch (Exception e) {
            log.error("Serialization failed: {}", e.getMessage());
        }
    }
}
