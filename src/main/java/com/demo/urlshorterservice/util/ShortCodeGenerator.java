package com.demo.urlshorterservice.util;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHGIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom secureRandom;

    public ShortCodeGenerator(){
      this.secureRandom = new SecureRandom();
    }

    // Generate a random 6-character Base62 code (0-9,a-z,A-Z).

    public String generate(){
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for(int i=0; i < CODE_LENGTH; i++){
            int randomIndex = secureRandom.nextInt(BASE62_CHARS.length());
            code.append(BASE62_CHARS.charAt(randomIndex));
        }
        return code.toString();
    }
}

