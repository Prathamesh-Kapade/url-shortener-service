package com.demo.urlshorterservice.security;

import com.demo.urlshorterservice.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityDsl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;


    public SecurityConfig(JwtFilter jwtFilter, UserRepository userRepository){
        this.jwtFilter=jwtFilter;
        this.userRepository = userRepository;
    }



    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository
                .findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails
                        .User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //  Public — no token needed
                         .requestMatchers("/").permitAll()              // root
                        .requestMatchers("/demo").permitAll()          // demo page
                        .requestMatchers("/demo.html").permitAll()     // static file
                        .requestMatchers("/index.html").permitAll()    // index
                        .requestMatchers("/static/**").permitAll()     // all static files
                        .requestMatchers("/*.html").permitAll()        // any .html file
                        .requestMatchers("/*.css", "/*.js").permitAll()// css/js
                        .requestMatchers("/api/auth/**").permitAll()   // register/login
                        .requestMatchers(HttpMethod.GET,
                                "/r/**").permitAll()


                        .requestMatchers("/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()// swagger

                        // ── Protected — JWT required ──────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/shorten").authenticated()
                        .requestMatchers("/api/analytics/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
