package com.demo.urlshorterservice.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    private final String shortCode;
    public ShortUrlNotFoundException(String shortCode) {
        super("Short URL not found for code: " + shortCode);
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}