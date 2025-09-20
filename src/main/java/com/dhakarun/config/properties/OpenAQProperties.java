package com.dhakarun.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource.openaq")
public record OpenAQProperties(String baseUrl, String apiKey, Duration timeout) {
}