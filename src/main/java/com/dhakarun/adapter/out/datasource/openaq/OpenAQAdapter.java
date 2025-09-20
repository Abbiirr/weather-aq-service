package com.dhakarun.adapter.out.datasource.openaq;

import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQResponse;
import com.dhakarun.application.port.out.AirQualityDataSource;
import com.dhakarun.domain.airquality.model.AQI;
import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.location.model.LocationId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OpenAQAdapter implements AirQualityDataSource {

    private final OpenAQClient client;

    public OpenAQAdapter(OpenAQClient client) {
        this.client = client;
    }

    @Override
    public Optional<AirQualityReading> fetchLatest(LocationId locationId) {
        return client.fetchLatest(locationId)
            .map(response -> toDomain(locationId, response));
    }

    private AirQualityReading toDomain(LocationId locationId, OpenAQResponse response) {
        return new AirQualityReading(locationId, new AQI(response.aqi()), List.of(), response.measuredAt());
    }
}
