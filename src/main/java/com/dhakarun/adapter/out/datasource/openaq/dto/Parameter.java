package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Parameter(
    @JsonProperty("id") Integer id,
    @JsonProperty("name") String name,
    @JsonProperty("units") String units,
    @JsonProperty("displayName") String displayName
) {}

