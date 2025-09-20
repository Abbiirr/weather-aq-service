package com.dhakarun.domain.running.model;

import com.dhakarun.domain.location.model.LocationId;
import java.util.Objects;

public record RunCondition(LocationId locationId, RunVerdict verdict, HealthRisk healthRisk) {

    public RunCondition {
        Objects.requireNonNull(locationId, "locationId");
        Objects.requireNonNull(verdict, "verdict");
        Objects.requireNonNull(healthRisk, "healthRisk");
    }
}
