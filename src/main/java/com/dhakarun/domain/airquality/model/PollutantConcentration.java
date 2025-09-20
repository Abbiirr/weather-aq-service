package com.dhakarun.domain.airquality.model;

import java.util.Objects;

public record PollutantConcentration(Pollutant pollutant, double microgramsPerCubicMeter) {

    public PollutantConcentration {
        Objects.requireNonNull(pollutant, "pollutant");
        if (microgramsPerCubicMeter < 0) {
            throw new IllegalArgumentException("Concentration must be non-negative");
        }
    }
}
