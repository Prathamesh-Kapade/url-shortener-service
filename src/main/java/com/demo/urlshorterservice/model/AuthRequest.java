package com.demo.urlshorterservice.model;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
