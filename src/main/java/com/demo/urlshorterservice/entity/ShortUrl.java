package com.demo.urlshorterservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls", uniqueConstraints = {
        @UniqueConstraint(columnNames = "shortCode")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
