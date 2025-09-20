package com.dhakarun.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource.openmeteo")
public record OpenMeteoProperties(String baseUrl, Duration timeout) {
}