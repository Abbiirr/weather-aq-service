package com.dhakarun.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "air_quality")
public class AirQualityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "aqi", nullable = false)
    private int aqi;

    @Column(name = "pollutant_summary", columnDefinition = "TEXT")
    private String pollutantSummary;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    protected AirQualityEntity() {
        // JPA only
    }

    public AirQualityEntity(Long id, String locationId, int aqi, String pollutantSummary, Instant measuredAt) {
        this.id = id;
        this.locationId = locationId;
        this.aqi = aqi;
        this.pollutantSummary = pollutantSummary;
        this.measuredAt = measuredAt;
    }

    public Long getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    public int getAqi() {
        return aqi;
    }

    public String getPollutantSummary() {
        return pollutantSummary;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
