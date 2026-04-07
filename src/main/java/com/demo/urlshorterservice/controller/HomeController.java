package com.demo.urlshorterservice.controller;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.demo.urlshorterservice.entity.ShortUrl;
import com.demo.urlshorterservice.model.ShortenUrlRequest;
import com.demo.urlshorterservice.model.ShortenUrlResponse;
import com.demo.urlshorterservice.service.AnalyticsService;
import com.demo.urlshorterservice.service.ClickEventProducer;
import com.demo.urlshorterservice.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;
    private final ClickEventProducer clickEventProducer;

    public HomeController(UrlShortenerService urlShortenerService,
                          AnalyticsService analyticsService,
                          ClickEventProducer clickEventProducer) {
        this.urlShortenerService = urlShortenerService;
        this.analyticsService    = analyticsService;
        this.clickEventProducer  = clickEventProducer;
    }
    @GetMapping("/")
    public String showForm(Model model){
        model.addAttribute("request", new ShortenUrlRequest());
        model.addAttribute("response", null);
        return "index";
    }

    @PostMapping("/shorten")
    public String shortenUrl(
            @Valid @ModelAttribute("request") ShortenUrlRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest){

        if(bindingResult.hasErrors()){
            model.addAttribute("response", null);
            return "index";
        }

        ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getOriginalUrl());

        String baseUrl = getBaseUrl(httpRequest);
        String shortUrlString = baseUrl + "/r/" + shortUrl.getShortCode();

        ShortenUrlResponse response = new ShortenUrlResponse();
        response.setShortCode(shortUrl.getShortCode());
        response.setShortUrl(shortUrlString);
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setExpiresAt(shortUrl.getExpiresAt());

        model.addAttribute("response", response);

        return "index";
    }

    @Operation(summary = "Redirect to original URL",
            description = "Public endpoint. Returns 302 redirect. No auth needed.")
    @GetMapping("/r/{shortCode}")
    public String redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);

        ClickEvent event = analyticsService.buildClickEvent(shortCode, request);

        clickEventProducer.publish(event);

        return "redirect:" + originalUrl;
    }

    @Operation(summary = "Shorten a URL",
            description = "Requires JWT. Returns shortCode and shortUrl.")
    @PostMapping("/api/shorten")
    @ResponseBody
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest req) {

        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";

        ShortUrl shortUrl = urlShortenerService
                .createShortUrl(req.getOriginalUrl());

        return ResponseEntity.ok(new ShortenUrlResponse(
                shortUrl.getShortCode(),
                "http://localhost:8081/r/" + shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt()
        ));
    }


    private String getBaseUrl(HttpServletRequest request){
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        if((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)){
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);

        return baseUrl.toString();
    }
}