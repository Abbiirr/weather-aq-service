package com.dhakarun.application.port.in;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;

public interface GetLocationDetailsUseCase {

    LocationDetailsView getDetails(LocationId locationId);

    record LocationDetailsView(
        Location location,
        AirQualityReading airQuality,
        WeatherReading weather,
        String runVerdict,
        String healthRisk
    ) {
    }
}
