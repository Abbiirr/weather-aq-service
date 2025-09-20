package com.dhakarun.application.port.in;

import com.dhakarun.domain.airquality.model.AQI;
import com.dhakarun.domain.airquality.model.PollutantConcentration;
import com.dhakarun.domain.location.model.LocationId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record IngestAirQualityCommand(
    LocationId locationId,
    AQI aqi,
    List<PollutantConcentration> pollutants,
    Instant measuredAt
) {
    public IngestAirQualityCommand {
        Objects.requireNonNull(locationId, "locationId");
        Objects.requireNonNull(aqi, "aqi");
        Objects.requireNonNull(pollutants, "pollutants");
        Objects.requireNonNull(measuredAt, "measuredAt");
    }
}
