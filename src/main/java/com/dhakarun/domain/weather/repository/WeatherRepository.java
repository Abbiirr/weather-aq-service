package com.dhakarun.domain.weather.repository;

import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;
import java.util.Optional;

public interface WeatherRepository {

    Optional<WeatherReading> findLatestByLocation(LocationId locationId);

    WeatherReading save(WeatherReading reading);
}
