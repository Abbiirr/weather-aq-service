package com.dhakarun.application.service;

import com.dhakarun.application.port.in.GetLocationDetailsUseCase;
import com.dhakarun.application.port.in.GetLocationSummaryUseCase;
import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.airquality.repository.AirQualityRepository;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.repository.LocationRepository;
import com.dhakarun.domain.running.model.RunCondition;
import com.dhakarun.domain.running.model.RunVerdict;
import com.dhakarun.domain.running.service.RunConditionEvaluator;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.repository.WeatherRepository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationQueryService implements GetLocationSummaryUseCase, GetLocationDetailsUseCase {

    private final LocationRepository locationRepository;
    private final AirQualityRepository airQualityRepository;
    private final WeatherRepository weatherRepository;
    private final RunConditionEvaluator runConditionEvaluator;
    private final DataIngestionService dataIngestionService;


    @Override
    public LocationSummaryView getSummary(LocationId locationId) {
        Location location = findLocation(locationId);
        AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
        WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);

        Readings readings = ensureReadings(locationId, airQuality, weather);
        airQuality = readings.airQuality();
        weather = readings.weather();

        Optional<RunCondition> runCondition = buildRunCondition(airQuality, weather);
        return new LocationSummaryView(
                locationId,
                location.getName(),
                airQuality != null ? airQuality.getAqi().value() : 0,
                weather != null ? weather.getTemperature().valueCelsius() : Double.NaN,
                weather != null ? weather.getHumidity().percentage() : Double.NaN,
                runCondition.map(rc -> rc.verdict().name()).orElse(RunVerdict.ACCEPTABLE.name()),
                runCondition.map(rc -> rc.healthRisk().message()).orElse("Data unavailable")
        );
    }

    @Override
    public LocationDetailsView getDetails(LocationId locationId) {
        Location location = findLocation(locationId);
        AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
        WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);

        Readings readings = ensureReadings(locationId, airQuality, weather);
        airQuality = readings.airQuality();
        weather = readings.weather();

        Optional<RunCondition> runCondition = buildRunCondition(airQuality, weather);
        return new LocationDetailsView(
                location,
                airQuality,
                weather,
                runCondition.map(rc -> rc.verdict().name()).orElse(RunVerdict.ACCEPTABLE.name()),
                runCondition.map(rc -> rc.healthRisk().message()).orElse("Data unavailable")
        );
    }

    private Location findLocation(LocationId locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId.value()));
    }

    private Readings ensureReadings(
            LocationId locationId,
            AirQualityReading currentAirQuality,
            WeatherReading currentWeather
    ) {
        if (currentAirQuality != null && currentWeather != null) {
            return new Readings(currentAirQuality, currentWeather);
        }

        var refreshResult = dataIngestionService.refreshFromDataSources(locationId);

        AirQualityReading refreshedAirQuality = currentAirQuality != null
                ? currentAirQuality
                : refreshResult.airQuality()
                .orElseGet(() -> airQualityRepository.findLatestByLocation(locationId).orElse(null));
        WeatherReading refreshedWeather = currentWeather != null
                ? currentWeather
                : refreshResult.weather()
                .orElseGet(() -> weatherRepository.findLatestByLocation(locationId).orElse(null));

        return new Readings(refreshedAirQuality, refreshedWeather);
    }

    private Optional<RunCondition> buildRunCondition(AirQualityReading airQuality, WeatherReading weather) {
        if (airQuality == null && weather == null) {
            return Optional.empty();
        }
        return Optional.of(runConditionEvaluator.evaluate(airQuality, weather));
    }

    public Page<LocationSummaryView> getAllSummaries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Location> locations = locationRepository.findAll(pageable);

        return locations.map(location -> {
            LocationId locationId = location.getId();
            AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
            WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);

            // Fetch if missing
            Readings readings = ensureReadings(locationId, airQuality, weather);
            airQuality = readings.airQuality();
            weather = readings.weather();

            Optional<RunCondition> runCondition = buildRunCondition(airQuality, weather);
            return new LocationSummaryView(
                    locationId,
                    location.getName(),
                    airQuality != null ? airQuality.getAqi().value() : 0,
                    weather != null ? weather.getTemperature().valueCelsius() : 0.0,
                    weather != null ? weather.getHumidity().percentage() : 0.0,
                    runCondition.map(rc -> rc.verdict().name()).orElse(RunVerdict.ACCEPTABLE.name()),
                    runCondition.map(rc -> rc.healthRisk().message()).orElse("Data unavailable")
            );
        });
    }

    private record Readings(
            AirQualityReading airQuality,
            WeatherReading weather
    ) {
    }
}
