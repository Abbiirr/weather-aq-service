package com.dhakarun.application.service;

import com.dhakarun.application.port.in.IngestAirQualityCommand;
import com.dhakarun.application.port.in.IngestWeatherCommand;
import com.dhakarun.application.port.out.AirQualityDataSource;
import com.dhakarun.application.port.out.WeatherDataSource;
import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.airquality.repository.AirQualityRepository;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.repository.WeatherRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DataIngestionService {

    private final AirQualityDataSource airQualityDataSource;
    private final WeatherDataSource weatherDataSource;
    private final AirQualityRepository airQualityRepository;
    private final WeatherRepository weatherRepository;

    public DataIngestionService(
        AirQualityDataSource airQualityDataSource,
        WeatherDataSource weatherDataSource,
        AirQualityRepository airQualityRepository,
        WeatherRepository weatherRepository
    ) {
        this.airQualityDataSource = airQualityDataSource;
        this.weatherDataSource = weatherDataSource;
        this.airQualityRepository = airQualityRepository;
        this.weatherRepository = weatherRepository;
    }

    public void ingestAirQuality(IngestAirQualityCommand command) {
        AirQualityReading reading = new AirQualityReading(
            command.locationId(),
            command.aqi(),
            command.pollutants(),
            command.measuredAt()
        );
        airQualityRepository.save(reading);
    }

    public void ingestWeather(IngestWeatherCommand command) {
        WeatherReading reading = new WeatherReading(
            command.locationId(),
            command.temperature(),
            command.humidity(),
            command.windData(),
            command.measuredAt()
        );
        weatherRepository.save(reading);
    }

    public RefreshResult refreshFromDataSources(LocationId locationId) {
        Optional<AirQualityReading> airQuality = airQualityDataSource.fetchLatest(locationId)
            .map(airQualityRepository::save);
        Optional<WeatherReading> weather = weatherDataSource.fetchLatest(locationId)
            .map(weatherRepository::save);
        return new RefreshResult(airQuality, weather);
    }

    // New overload to avoid referencing LocationId in callers
    public RefreshResult refreshFromDataSources(String locationId) {
        return refreshFromDataSources(new LocationId(locationId));
    }

    public record RefreshResult(
        Optional<AirQualityReading> airQuality,
        Optional<WeatherReading> weather
    ) {
    }
}
