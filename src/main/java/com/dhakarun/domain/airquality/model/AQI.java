package com.dhakarun.domain.airquality.model;

public record AQI(int value) {

    public AQI {
        if (value < 0) {
            throw new IllegalArgumentException("AQI cannot be negative");
        }
    }

    public AQICategory category() {
        int v = value;
        if (v <= 50) return AQICategory.GOOD;
        if (v <= 100) return AQICategory.MODERATE;
        if (v <= 150) return AQICategory.UNHEALTHY_FOR_SENSITIVE;
        if (v <= 200) return AQICategory.UNHEALTHY;
        if (v <= 300) return AQICategory.VERY_UNHEALTHY;
        return AQICategory.HAZARDOUS;
    }
}
