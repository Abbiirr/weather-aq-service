package com.dhakarun.application.port.in;

import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.Humidity;
import com.dhakarun.domain.weather.model.Temperature;
import com.dhakarun.domain.weather.model.WindData;
import java.time.Instant;
import java.util.Objects;

public record IngestWeatherCommand(
    LocationId locationId,
    Temperature temperature,
    Humidity humidity,
    WindData windData,
    Instant measuredAt
) {
    public IngestWeatherCommand {
        Objects.requireNonNull(locationId, "locationId");
        Objects.requireNonNull(temperature, "temperature");
        Objects.requireNonNull(humidity, "humidity");
        Objects.requireNonNull(windData, "windData");
        Objects.requireNonNull(measuredAt, "measuredAt");
    }
}
