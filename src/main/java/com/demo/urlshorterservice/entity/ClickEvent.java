package com.demo.urlshorterservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    private String ipAddress;
    private String country;
    private String city;
    private String device;
    private String browser;
    private String os;
    private String referer;

    @PrePersist
    protected void onCreate(){
        this.clickedAt = LocalDateTime.now();
    }
}
