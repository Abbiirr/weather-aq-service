package com.dhakarun.adapter.out.persistence.mapper;

import com.dhakarun.adapter.out.persistence.entity.AirQualityEntity;
import com.dhakarun.adapter.out.persistence.entity.LocationEntity;
import com.dhakarun.adapter.out.persistence.entity.WeatherEntity;
import com.dhakarun.domain.airquality.model.AQI;
import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.airquality.model.Pollutant;
import com.dhakarun.domain.airquality.model.PollutantConcentration;
import com.dhakarun.domain.location.model.Coordinates;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.model.LocationType;
import com.dhakarun.domain.weather.model.Humidity;
import com.dhakarun.domain.weather.model.Temperature;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.model.WindData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public Location toDomain(LocationEntity entity) {
        return new Location(
            new LocationId(entity.getId()),
            entity.getName(),
            new Coordinates(entity.getLatitude(), entity.getLongitude()),
            LocationType.valueOf(entity.getType())
        );
    }

    public LocationEntity toEntity(Location location) {
        return new LocationEntity(
            location.getId().value(),
            location.getName(),
            location.getType().name(),
            location.getCoordinates().latitude(),
            location.getCoordinates().longitude()
        );
    }

    public AirQualityReading toDomain(AirQualityEntity entity) {
        return new AirQualityReading(
            new LocationId(entity.getLocationId()),
            new AQI(entity.getAqi()),
            parsePollutantSummary(entity.getPollutantSummary()),
            entity.getMeasuredAt()
        );
    }

    public AirQualityEntity toEntity(AirQualityReading reading) {
        String summary = reading.getPollutants().stream()
            .map(c -> c.pollutant().name() + "=" + c.microgramsPerCubicMeter())
            .collect(Collectors.joining(","));
        return new AirQualityEntity(
            null,
            reading.getLocationId().value(),
            reading.getAqi().value(),
            summary,
            reading.getMeasuredAt()
        );
    }

    public WeatherReading toDomain(WeatherEntity entity) {
        return new WeatherReading(
            new LocationId(entity.getLocationId()),
            new Temperature(entity.getTemperatureCelsius()),
            new Humidity(entity.getHumidityPercentage()),
            new WindData(entity.getWindSpeedMetersPerSecond(), entity.getWindDirection()),
            entity.getMeasuredAt()
        );
    }

    public WeatherEntity toEntity(WeatherReading reading) {
        WindData windData = reading.getWindData();
        return new WeatherEntity(
            null,
            reading.getLocationId().value(),
            reading.getTemperature().valueCelsius(),
            reading.getHumidity().percentage(),
            windData.speedMetersPerSecond(),
            windData.direction(),
            reading.getMeasuredAt()
        );
    }

    private List<PollutantConcentration> parsePollutantSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return List.of();
        }
        List<PollutantConcentration> list = new ArrayList<>();
        for (String token : summary.split(",")) {
            String part = token.trim();
            if (part.isEmpty()) continue;
            String[] kv = part.contains("=") ? part.split("=", 2) : part.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            String val = kv[1].trim();
            try {
                Pollutant pollutant = Pollutant.valueOf(key);
                double value = Double.parseDouble(val);
                if (value >= 0) {
                    list.add(new PollutantConcentration(pollutant, value));
                }
            } catch (Exception ignored) {
                // skip unknown pollutant names or invalid numbers
            }
        }
        return List.copyOf(list);
    }
}
