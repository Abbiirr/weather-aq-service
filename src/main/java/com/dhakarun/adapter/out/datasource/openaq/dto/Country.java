package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Country(
    @JsonProperty("id") Integer id,
    @JsonProperty("code") String code,
    @JsonProperty("name") String name
) {}

