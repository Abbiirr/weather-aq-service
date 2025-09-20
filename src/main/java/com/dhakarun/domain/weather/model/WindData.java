package com.dhakarun.domain.weather.model;

import java.util.Objects;

public record WindData(double speedMetersPerSecond, String direction) {

    public WindData {
        Objects.requireNonNull(direction, "direction");
        if (speedMetersPerSecond < 0) {
            throw new IllegalArgumentException("Wind speed must be non-negative");
        }
    }
}
