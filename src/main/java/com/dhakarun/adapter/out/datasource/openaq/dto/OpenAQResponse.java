package com.dhakarun.adapter.out.datasource.openaq.dto;

import java.time.Instant;

public record OpenAQResponse(int aqi, Instant measuredAt) {
}
