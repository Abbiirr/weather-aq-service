package com.dhakarun.application.port.in;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;
import java.util.Optional;

public interface RefreshLocationDataUseCase {

    RefreshResult refresh(LocationId locationId);

    default RefreshResult refresh(String locationId) {
        return refresh(new LocationId(locationId));
    }

    record RefreshResult(Optional<AirQualityReading> airQuality, Optional<WeatherReading> weather) {
        public RefreshResult {
            airQuality = airQuality == null ? Optional.empty() : airQuality;
            weather = weather == null ? Optional.empty() : weather;
        }
    }
}
