package com.dhakarun.domain.airquality.repository;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.location.model.LocationId;
import java.util.Optional;

public interface AirQualityRepository {

    Optional<AirQualityReading> findLatestByLocation(LocationId locationId);

    AirQualityReading save(AirQualityReading reading);
}
