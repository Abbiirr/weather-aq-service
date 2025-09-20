package com.dhakarun.application.port.out;

import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;
import java.util.Optional;

public interface WeatherDataSource {

    Optional<WeatherReading> fetchLatest(LocationId locationId);
}
