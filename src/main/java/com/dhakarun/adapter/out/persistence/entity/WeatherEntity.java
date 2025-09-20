package com.dhakarun.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "weather")
public class WeatherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "temperature_celsius", nullable = false)
    private double temperatureCelsius;

    @Column(name = "humidity_percentage", nullable = false)
    private double humidityPercentage;

    @Column(name = "wind_speed_mps", nullable = false)
    private double windSpeedMetersPerSecond;

    @Column(name = "wind_direction", nullable = false)
    private String windDirection;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    protected WeatherEntity() {
        // JPA only
    }

    public WeatherEntity(
        Long id,
        String locationId,
        double temperatureCelsius,
        double humidityPercentage,
        double windSpeedMetersPerSecond,
        String windDirection,
        Instant measuredAt
    ) {
        this.id = id;
        this.locationId = locationId;
        this.temperatureCelsius = temperatureCelsius;
        this.humidityPercentage = humidityPercentage;
        this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
        this.windDirection = windDirection;
        this.measuredAt = measuredAt;
    }

    public Long getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getHumidityPercentage() {
        return humidityPercentage;
    }

    public double getWindSpeedMetersPerSecond() {
        return windSpeedMetersPerSecond;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
