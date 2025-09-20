package com.dhakarun.domain.weather.model;

import com.dhakarun.domain.location.model.LocationId;
import java.time.Instant;
import java.util.Objects;

public final class WeatherReading {

    private final LocationId locationId;
    private final Temperature temperature;
    private final Humidity humidity;
    private final WindData windData;
    private final Instant measuredAt;

    public WeatherReading(LocationId locationId, Temperature temperature, Humidity humidity, WindData windData, Instant measuredAt) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.temperature = Objects.requireNonNull(temperature, "temperature");
        this.humidity = Objects.requireNonNull(humidity, "humidity");
        this.windData = Objects.requireNonNull(windData, "windData");
        this.measuredAt = Objects.requireNonNull(measuredAt, "measuredAt");
    }

    public LocationId getLocationId() {
        return locationId;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public WindData getWindData() {
        return windData;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
