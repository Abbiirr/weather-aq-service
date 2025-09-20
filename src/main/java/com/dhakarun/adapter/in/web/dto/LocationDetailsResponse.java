package com.dhakarun.adapter.in.web.dto;

public record LocationDetailsResponse(
    String locationId,
    String name,
    String type,
    double latitude,
    double longitude,
    Integer aqiValue,
    Double temperatureCelsius,
    Double humidityPercentage,
    Double windSpeedMetersPerSecond,
    String windDirection,
    String runVerdict,
    String healthRisk
) {
}
