package com.dhakarun.domain.airquality.model;

import com.dhakarun.domain.location.model.LocationId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class AirQualityReading {

    private final LocationId locationId;
    private final AQI aqi;
    private final List<PollutantConcentration> pollutants;
    private final Instant measuredAt;

    public AirQualityReading(LocationId locationId, AQI aqi, List<PollutantConcentration> pollutants, Instant measuredAt) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.aqi = Objects.requireNonNull(aqi, "aqi");
        this.pollutants = List.copyOf(Objects.requireNonNull(pollutants, "pollutants"));
        this.measuredAt = Objects.requireNonNull(measuredAt, "measuredAt");
    }

    public LocationId getLocationId() {
        return locationId;
    }

    public AQI getAqi() {
        return aqi;
    }

    public List<PollutantConcentration> getPollutants() {
        return pollutants;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
