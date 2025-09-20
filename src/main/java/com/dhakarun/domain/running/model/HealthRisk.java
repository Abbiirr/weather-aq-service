package com.dhakarun.domain.running.model;

import java.util.Objects;

public record HealthRisk(String message) {

    public HealthRisk {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Health risk message cannot be blank");
        }
    }

    public static HealthRisk none() {
        return new HealthRisk("No significant health risks detected");
    }
}
