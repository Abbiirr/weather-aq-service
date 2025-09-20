package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAQLatestMeasurement(
    @JsonProperty("datetime") OpenAQMeasurementTimestamp datetime,
    @JsonProperty("value") Double value,
    @JsonProperty("coordinates") Coordinates coordinates,
    @JsonProperty("sensorsId") Integer sensorsId,
    @JsonProperty("locationsId") Integer locationsId
) {}
