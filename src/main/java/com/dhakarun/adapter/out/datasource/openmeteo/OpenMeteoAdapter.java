package com.dhakarun.adapter.out.datasource.openmeteo;

import com.dhakarun.adapter.out.datasource.openmeteo.dto.OpenMeteoResponse;
import com.dhakarun.application.port.out.WeatherDataSource;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.Humidity;
import com.dhakarun.domain.weather.model.Temperature;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.model.WindData;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OpenMeteoAdapter implements WeatherDataSource {

    private final OpenMeteoClient client;

    public OpenMeteoAdapter(OpenMeteoClient client) {
        this.client = client;
    }

    @Override
    public Optional<WeatherReading> fetchLatest(LocationId locationId) {
        return client.fetchLatest(locationId)
            .map(response -> toDomain(locationId, response));
    }

    private WeatherReading toDomain(LocationId locationId, OpenMeteoResponse response) {
        return new WeatherReading(
            locationId,
            new Temperature(response.temperatureCelsius()),
            new Humidity(response.humidityPercentage()),
            new WindData(response.windSpeedMetersPerSecond(), response.windDirection()),
            response.measuredAt()
        );
    }
}
