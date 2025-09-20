package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAQLocationDetail(
    @JsonProperty("id") Integer id,
    @JsonProperty("name") String name,
    @JsonProperty("locality") String locality,
    @JsonProperty("city") String city,
    @JsonProperty("country") Country country,
    @JsonProperty("coordinates") Coordinates coordinates,
    @JsonProperty("provider") Provider provider,
    @JsonProperty("isMobile") Boolean isMobile,
    @JsonProperty("isMonitor") Boolean isMonitor,
    @JsonProperty("instruments") List<Instrument> instruments
) {}