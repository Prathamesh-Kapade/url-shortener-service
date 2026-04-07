package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.demo.urlshorterservice.model.GeoData;
import com.demo.urlshorterservice.repository.ClickEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;


@Service
@Slf4j
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepo;
    private final GeoIpService geoIpService;
    private final ObjectMapper objectMapper;

    public ClickEventConsumer(ClickEventRepository clickEventRepo,
                              GeoIpService geoIpService,
                              ObjectMapper objectMapper) {
        this.clickEventRepo = clickEventRepo;
        this.geoIpService   = geoIpService;
        this.objectMapper   = objectMapper;
    }

    @KafkaListener(topics = "click-events", groupId = "analytics-group")
    public void consume(byte[] message, Acknowledgment ack) {
        try {
            ClickEvent event = objectMapper.readValue(message, ClickEvent.class);

            GeoData geo = geoIpService.lookup(event.getIpAddress());
            event.setCountry(geo.getCountry());
            event.setCity(geo.getCity());

            clickEventRepo.save(event);
            ack.acknowledge();
            log.info("Saved click event: shortCode={} country={} device={}",
                    event.getShortCode(), event.getCountry(), event.getDevice());

        } catch (Exception e) {
            log.error("Failed to process click event: {}", e.getMessage(), e);
        }
    }
}