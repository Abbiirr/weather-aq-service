package com.dhakarun.adapter.out.datasource.openaq;

import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLatestMeasurement;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLatestMeasurementsResponse;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLocation;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLocationDetail;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLocationDetailResponse;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLocationsResponse;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQResponse;
import com.dhakarun.config.properties.OpenAQProperties;
import com.dhakarun.domain.location.model.LocationId;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenAQClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAQClient.class);
    private static final String OPENAQ_ID_PREFIX = "openaq-";

    private final RestTemplate restTemplate;
    private final OpenAQProperties properties;

    public OpenAQClient(RestTemplateBuilder restTemplateBuilder, OpenAQProperties properties) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
            .connectTimeout(properties.timeout())
            .readTimeout(properties.timeout())
            .build();
    }

    public Optional<OpenAQResponse> fetchLatest(LocationId locationId) {
        Optional<Integer> openAqLocationId = resolveOpenAqLocationId(locationId);
        if (openAqLocationId.isEmpty()) {
            log.warn("Unsupported OpenAQ location id format: {}", locationId.value());
            return Optional.empty();
        }

        Integer numericId = openAqLocationId.get();
        String url = UriComponentsBuilder.fromHttpUrl(properties.baseUrl() + "/locations/" + numericId + "/latest")
            .toUriString();

        try {
            log.debug("Fetching latest air quality from OpenAQ for location {}", numericId);
            ResponseEntity<OpenAQLatestMeasurementsResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(defaultHeaders()), OpenAQLatestMeasurementsResponse.class);

            OpenAQLatestMeasurementsResponse body = response.getBody();
            if (body == null || body.results() == null || body.results().isEmpty()) {
                log.debug("No latest measurements returned for OpenAQ location {}", numericId);
                return Optional.empty();
            }

            List<MeasurementSample> samples = body.results().stream()
                .filter(measurement -> measurement.value() != null)
                .filter(measurement -> measurement.datetime() != null && measurement.datetime().utc() != null)
                .map(measurement -> new MeasurementSample(measurement, parseInstant(measurement.datetime().utc())))
                .filter(sample -> sample.timestamp() != null)
                .filter(sample -> Double.isFinite(sample.measurement().value()))
                .filter(sample -> sample.measurement().value() >= 0)
                .toList();

            if (samples.isEmpty()) {
                log.debug("Latest measurements for OpenAQ location {} contain no usable datapoints", numericId);
                return Optional.empty();
            }

            Instant measuredAt = samples.stream()
                .map(MeasurementSample::timestamp)
                .max(Comparator.naturalOrder())
                .orElse(null);

            if (measuredAt == null) {
                log.debug("Unable to resolve measurement timestamp for OpenAQ location {}", numericId);
                return Optional.empty();
            }

            List<MeasurementSample> latestSamples = samples.stream()
                .filter(sample -> sample.timestamp().equals(measuredAt))
                .toList();

            if (latestSamples.isEmpty()) {
                log.debug("No measurement values aligned with timestamp {} for OpenAQ location {}", measuredAt, numericId);
                return Optional.empty();
            }

            int aqi = latestSamples.stream()
                .mapToInt(sample -> (int) Math.round(sample.measurement().value()))
                .max()
                .orElse(0);

            return Optional.of(new OpenAQResponse(aqi, measuredAt));
        } catch (Exception e) {
            log.error("Failed to fetch latest air quality from OpenAQ for location {}", numericId, e);
        }

        return Optional.empty();
    }

    public List<OpenAQLocation> fetchLocationsByCity(String city, int limit) {
        // OpenAQ v3 API doesn't support city parameter directly
        // Strategy:
        // 1. For known cities like Dhaka, use country filtering + client-side city filter
        // 2. Otherwise, fetch broader results and filter client-side

        String normalizedCity = city.trim().toLowerCase();

        // Special handling for known cities
        Integer countryId = null;
        if (normalizedCity.contains("dhaka")) {
            countryId = 128; // Bangladesh
        } else if (normalizedCity.contains("delhi") || normalizedCity.contains("mumbai") ||
                   normalizedCity.contains("bangalore") || normalizedCity.contains("kolkata")) {
            countryId = 9; // India
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(properties.baseUrl() + "/locations");

        if (countryId != null) {
            // Use country filter for better performance
            uriBuilder.queryParam("countries_id", countryId);
            uriBuilder.queryParam("limit", 500); // Get more results from specific country
        } else {
            // Fallback to fetching more results globally
            uriBuilder.queryParam("limit", 1000);
        }

        String url = uriBuilder.toUriString();

        try {
            log.info("Fetching locations from OpenAQ API v3 for city '{}' (country_id: {})", city, countryId);
            ResponseEntity<OpenAQLocationsResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(defaultHeaders()), OpenAQLocationsResponse.class);

            if (response.getBody() != null && response.getBody().results() != null) {
                // Filter by city name (case-insensitive partial match)
                List<OpenAQLocation> filteredLocations = response.getBody().results().stream()
                    .filter(location -> {
                        if (location.city() == null) return false;
                        String locationCity = location.city().toLowerCase();
                        // Check if location city contains search term or vice versa
                        return locationCity.contains(normalizedCity) || normalizedCity.contains(locationCity);
                    })
                    .limit(limit)
                    .toList();

                log.info("Found {} locations for city '{}' from {} total results",
                    filteredLocations.size(), city, response.getBody().results().size());

                // If no results found with city filter, try location name as fallback
                if (filteredLocations.isEmpty() && response.getBody().results() != null) {
                    filteredLocations = response.getBody().results().stream()
                        .filter(location -> {
                            if (location.name() == null) return false;
                            return location.name().toLowerCase().contains(normalizedCity);
                        })
                        .limit(limit)
                        .toList();

                    if (!filteredLocations.isEmpty()) {
                        log.info("Found {} locations by name match for '{}'", filteredLocations.size(), city);
                    }
                }

                return filteredLocations;
            }
        } catch (Exception e) {
            log.error("Failed to fetch locations from OpenAQ for city: {}", city, e);
        }

        return List.of();
    }

    public Optional<OpenAQLocationDetail> fetchLocationById(Integer locationId) {
        String url = properties.baseUrl() + "/locations/" + locationId;

        try {
            log.debug("Fetching location details from OpenAQ API v3 for id: {}", locationId);
            ResponseEntity<OpenAQLocationDetailResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(defaultHeaders()), OpenAQLocationDetailResponse.class);

            if (response.getBody() != null && response.getBody().results() != null && !response.getBody().results().isEmpty()) {
                // Use stream.findFirst() to avoid direct indexed access
                return response.getBody().results().stream().findFirst();
            }
        } catch (Exception e) {
            log.error("Failed to fetch location details from OpenAQ", e);
        }

        return Optional.empty();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
            headers.set("X-API-Key", properties.apiKey());
        }
        return headers;
    }

    private Optional<Integer> resolveOpenAqLocationId(LocationId locationId) {
        String raw = locationId.value();
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String candidate = raw;
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.startsWith(OPENAQ_ID_PREFIX)) {
            int dash = raw.indexOf('-');
            if (dash < 0 || dash == raw.length() - 1) {
                return Optional.empty();
            }
            candidate = raw.substring(dash + 1);
        }

        try {
            return Optional.of(Integer.valueOf(candidate));
        } catch (NumberFormatException e) {
            log.debug("Location id {} does not contain a numeric OpenAQ id", raw, e);
            return Optional.empty();
        }
    }

    private Instant parseInstant(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse OpenAQ timestamp: {}", timestamp, e);
            return null;
        }
    }

    private record MeasurementSample(OpenAQLatestMeasurement measurement, Instant timestamp) {}
}
