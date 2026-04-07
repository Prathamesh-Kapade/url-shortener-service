package com.demo.urlshorterservice.model;

public class GeoData {

    private final String country;
    private final String city;

    public GeoData(String country, String city) {
        this.country = country;
        this.city    = city;
    }

    public static GeoData of(String country, String city) {
        return new GeoData(country, city);
    }

    public static GeoData unknown() {
        return new GeoData("Unknown", "Unknown");
    }

    public String getCountry() { return country; }
    public String getCity()    { return city;    }
}