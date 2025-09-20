package com.dhakarun.application.port.out;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.location.model.LocationId;
import java.util.Optional;

public interface AirQualityDataSource {

    Optional<AirQualityReading> fetchLatest(LocationId locationId);
}
