package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAQLocationDetailResponse(
    @JsonProperty("results") List<OpenAQLocationDetail> results,
    @JsonProperty("meta") Meta meta
) {}

