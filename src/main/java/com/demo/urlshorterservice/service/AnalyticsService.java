package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.entity.ClickEvent;
import com.demo.urlshorterservice.model.GeoData;
import com.demo.urlshorterservice.repository.ClickEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepo;
    private final GeoIpService geoIpService;


    public AnalyticsService(ClickEventRepository clickEventRepo,
                            GeoIpService geoIpService) {
        this.clickEventRepo = clickEventRepo;
        this.geoIpService   = geoIpService;
    }

    public ClickEvent buildClickEvent(String shortCode,
                                      HttpServletRequest request) {
        String ip        = extractIp(request);
        String userAgent = request.getHeader("User-Agent");

        GeoData geo = geoIpService.lookup(ip);
        String country = (geo != null) ? geo.getCountry() : "Unknown";
        String city    = (geo != null) ? geo.getCity()    : "Unknown";

        return new ClickEvent(
                null,
                shortCode,
                null,
                maskIp(ip),
                country,
                city,
                parseDevice(userAgent),
                parseBrowser(userAgent),
                parseOs(userAgent),
                request.getHeader("Referer")
        );
    }


    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String maskIp(String ip){
        if(ip == null) return null;
        int lastDot= ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0,lastDot) + ".0" : ip;
    }

    private String parseDevice(String ua){
        if(ua == null) return "Unknown";
        if(ua.contains("Mobile") || ua.contains("Android"))
            return "Mobile";
        if(ua.contains("Tablet") || ua.contains("iPad"))
            return "Tablet";
        return "Desktop";
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Chrome") && !ua.contains("Edg"))
            return "Chrome";
        if (ua.contains("Firefox"))  return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome"))
            return "Safari";
        if (ua.contains("Edg"))      return "Edge";
        return "Other";
    }

    private String parseOs(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad"))
            return "iOS";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Linux"))    return "Linux";
        return "Other";
    }
}
