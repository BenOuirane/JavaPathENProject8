package com.openclassrooms.utils;

public class Attraction {
    private String name;
    private double latitude;
    private double longitude;

    // Constructor
    public Attraction(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
