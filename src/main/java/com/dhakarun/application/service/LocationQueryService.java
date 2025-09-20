package com.dhakarun.application.service;

import com.dhakarun.application.port.in.BrowseLocationsUseCase;
import com.dhakarun.application.port.in.GetLocationDetailsUseCase;
import com.dhakarun.application.port.in.GetLocationSummaryUseCase;
import com.dhakarun.application.port.in.ListLocationsUseCase;
import com.dhakarun.application.port.in.RefreshLocationDataUseCase;
import com.dhakarun.application.port.out.LocationReadPort;
import com.dhakarun.application.shared.PageQuery;
import com.dhakarun.application.shared.PageResult;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationQueryService implements
    GetLocationSummaryUseCase,
    GetLocationDetailsUseCase,
    BrowseLocationsUseCase,
    ListLocationsUseCase {

    private final LocationRepository locationRepository;
    private final AirQualityRepository airQualityRepository;
    private final WeatherRepository weatherRepository;
    private final RunConditionEvaluator runConditionEvaluator;
    private final RefreshLocationDataUseCase refreshLocationDataUseCase;
    private final LocationReadPort locationReadPort;

    @Override
    public GetLocationSummaryUseCase.LocationSummaryView getSummary(LocationId locationId) {
        Location location = findLocation(locationId);
        return buildSummaryView(locationId, location.getName());
    }

    @Override
    public GetLocationDetailsUseCase.LocationDetailsView getDetails(LocationId locationId) {
        Location location = findLocation(locationId);
        AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
        WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);

        Readings readings = ensureReadings(locationId, airQuality, weather);
        airQuality = readings.airQuality();
        weather = readings.weather();

        Optional<RunCondition> runCondition = buildRunCondition(airQuality, weather);
        return new GetLocationDetailsUseCase.LocationDetailsView(
            location,
            airQuality,
            weather,
            runCondition.map(rc -> rc.verdict().name()).orElse(RunVerdict.ACCEPTABLE.name()),
            runCondition.map(rc -> rc.healthRisk().message()).orElse("Data unavailable")
        );
    }

    @Override
    public PageResult<Location> list(PageQuery query) {
        return locationReadPort.fetchPage(query);
    }

    @Override
    public PageResult<GetLocationSummaryUseCase.LocationSummaryView> browse(PageQuery query) {
        var locationsPage = locationReadPort.fetchPage(query);
        var content = locationsPage.content().stream()
            .map(location -> buildSummaryView(location.getId(), location.getName()))
            .toList();
        return new PageResult<>(
            locationsPage.page(),
            locationsPage.size(),
            locationsPage.totalElements(),
            locationsPage.hasNext(),
            content
        );
    }

    private GetLocationSummaryUseCase.LocationSummaryView buildSummaryView(LocationId locationId, String locationName) {
        AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
        WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);

        Readings readings = ensureReadings(locationId, airQuality, weather);
        airQuality = readings.airQuality();
        weather = readings.weather();

        Optional<RunCondition> runCondition = buildRunCondition(airQuality, weather);
        return new GetLocationSummaryUseCase.LocationSummaryView(
            locationId,
            locationName,
            airQuality != null ? airQuality.getAqi().value() : 0,
            weather != null ? weather.getTemperature().valueCelsius() : Double.NaN,
            weather != null ? weather.getHumidity().percentage() : Double.NaN,
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

        var refreshResult = refreshLocationDataUseCase.refresh(locationId);

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

    private record Readings(
        AirQualityReading airQuality,
        WeatherReading weather
    ) {
    }
}




