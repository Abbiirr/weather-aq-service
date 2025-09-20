package com.dhakarun.adapter.out.datasource.openmeteo.dto;

import java.time.Instant;

public record OpenMeteoResponse(
    double temperatureCelsius,
    double humidityPercentage,
    double windSpeedMetersPerSecond,
    String windDirection,
    Instant measuredAt
) {
}
