package com.dhakarun.adapter.in.web.mapper;

import com.dhakarun.adapter.in.web.dto.LocationDetailsResponse;
import com.dhakarun.adapter.in.web.dto.LocationSummaryResponse;
import com.dhakarun.application.port.in.GetLocationDetailsUseCase;
import com.dhakarun.application.port.in.GetLocationSummaryUseCase;
import org.springframework.stereotype.Component;

@Component
public class LocationDtoMapper {

    public LocationSummaryResponse toSummary(GetLocationSummaryUseCase.LocationSummaryView view) {
        return new LocationSummaryResponse(
            view.locationId().value(),
            view.name(),
            view.aqiValue(),
            view.temperatureCelsius(),
            view.humidityPercentage(),
            view.runVerdict(),
            view.healthRisk()
        );
    }

    public LocationDetailsResponse toDetails(GetLocationDetailsUseCase.LocationDetailsView view) {
        var location = view.location();
        var coordinates = location.getCoordinates();
        var airQuality = view.airQuality();
        var weather = view.weather();
        return new LocationDetailsResponse(
            location.getId().value(),
            location.getName(),
            location.getType().name(),
            coordinates.latitude(),
            coordinates.longitude(),
            airQuality != null ? airQuality.getAqi().value() : null,
            weather != null ? weather.getTemperature().valueCelsius() : null,
            weather != null ? weather.getHumidity().percentage() : null,
            weather != null ? weather.getWindData().speedMetersPerSecond() : null,
            weather != null ? weather.getWindData().direction() : null,
            view.runVerdict(),
            view.healthRisk()
        );
    }
}
