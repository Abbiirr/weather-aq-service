package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAQLatestMeasurementsResponse(
    @JsonProperty("meta") Meta meta,
    @JsonProperty("results") List<OpenAQLatestMeasurement> results
) {}
