package com.demo.urlshorterservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortenUrlRequest {

    @NotBlank(message = "URL cannot be empty")
    @Pattern(
            regexp = "^(https?://)[\\w\\-]+(\\.[\\w\\-]+)+.*$",
            message = "Must be a valid URL starting with http:// or https://"
    )
    private String originalUrl;

    private java.time.LocalDateTime expiresAt;
}
