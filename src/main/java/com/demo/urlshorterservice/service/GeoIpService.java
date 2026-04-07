package com.demo.urlshorterservice.service;

import com.demo.urlshorterservice.model.GeoData;
import io.lettuce.core.dynamic.annotation.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class GeoIpService {


    private static final String GEO_API =
            "http://ip-api.com/json/{ip}?fields=country,city,status";

    private final RestTemplate restTemplate = new RestTemplate();

    public GeoData lookup(String ip) {
        try {
            if (isPrivateIp(ip)) return GeoData.unknown();

            String url = "http://ip-api.com/json/" + ip
                    + "?fields=country,city,status";
            RestTemplate rest = new RestTemplate();
            Map<?,?> resp = rest.getForObject(url, Map.class);

            if (resp != null && "success".equals(resp.get("status"))) {
                return new GeoData(
                        (String) resp.get("country"),
                        (String) resp.get("city")
                );
            }
        } catch (Exception e) {
            // log and fall through
        }
        return GeoData.unknown();
    }

    private boolean isPrivateIp(String ip) {
        return ip == null
                || ip.startsWith("127.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("10.")
                || ip.equals("::1");
    }
}



