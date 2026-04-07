package com.demo.urlshorterservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("""
                    Production-grade URL Shortener with:
                    - JWT Authentication
                    - Redis Caching (Cache-Aside pattern)
                    - Async Analytics via Kafka
                      - GeoIP tracking

                    **How to use:**
                    1. Register via POST /api/auth/register
                    2. Login via POST /api/auth/login — copy the token
                    3. Click Authorize button → paste: Bearer {token}
                    4. Use POST /api/shorten to create short URLs
                    5. GET /r/{code} to test the redirect
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Prathamesh")
                                .url("https://github.com/your-username")))


                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here")));
    }
}