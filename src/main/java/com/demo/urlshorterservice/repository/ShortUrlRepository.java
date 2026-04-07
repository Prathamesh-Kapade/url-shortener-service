package com.demo.urlshorterservice.repository;

import com.demo.urlshorterservice.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByOriginalUrl(String originalUrl);

    Optional<ShortUrl> findByShortCode(String shortCode);
}
