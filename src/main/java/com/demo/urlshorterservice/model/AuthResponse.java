package com.demo.urlshorterservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private  String token;
    private  String username;
    private long expiresIn;

}
