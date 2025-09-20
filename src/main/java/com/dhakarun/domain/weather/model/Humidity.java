package com.dhakarun.domain.weather.model;

public record Humidity(double percentage) {

    public Humidity {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Humidity must be between 0 and 100");
        }
    }
}
