package com.dhakarun.application.port.in;

import com.dhakarun.domain.location.model.LocationId;

public interface GetLocationSummaryUseCase {

    LocationSummaryView getSummary(LocationId locationId);

    record LocationSummaryView(
        LocationId locationId,
        String name,
        int aqiValue,
        double temperatureCelsius,
        double humidityPercentage,
        String runVerdict,
        String healthRisk
    ) {
    }
}
