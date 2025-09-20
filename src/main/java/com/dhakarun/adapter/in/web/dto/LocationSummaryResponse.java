package com.dhakarun.adapter.in.web.dto;

public record LocationSummaryResponse(
    String locationId,
    String name,
    int aqiValue,
    double temperatureCelsius,
    double humidityPercentage,
    String runVerdict,
    String healthRisk
) {
}
