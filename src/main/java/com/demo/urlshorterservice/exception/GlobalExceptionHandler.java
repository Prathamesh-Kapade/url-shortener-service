package com.demo.urlshorterservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(
            ShortUrlNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(Map.of("error", "Short URL not found",
                        "code", ex.getShortCode()));
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Void> handleRedisFailure() {
        log.error("Redis unavailable — serving from DB");
        return null;
    }
}