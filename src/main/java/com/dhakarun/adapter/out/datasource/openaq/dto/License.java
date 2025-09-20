package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record License(
    @JsonProperty("id") Integer id,
    @JsonProperty("name") String name,
    @JsonProperty("attribution") Attribution attribution,
    @JsonProperty("dateFrom") String dateFrom,
    @JsonProperty("dateTo") String dateTo
) {}

