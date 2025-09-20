package com.dhakarun.application.bootstrap;

import com.dhakarun.adapter.out.datasource.openaq.OpenAQClient;
import com.dhakarun.adapter.out.datasource.openaq.dto.OpenAQLocation;
import com.dhakarun.domain.location.model.Coordinates;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.model.LocationType;
import com.dhakarun.domain.location.repository.LocationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class LocationBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocationBootstrapService.class);

    private final LocationRepository locationRepository;
    private final OpenAQClient openAQClient;

    @Value("${app.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.city:Dhaka}")
    private String city;

    @Value("${app.bootstrap.location-limit:1000}")
    private int locationLimit;

    @Value("${app.bootstrap.force-refresh:false}")
    private boolean forceRefresh;

    public LocationBootstrapService(LocationRepository locationRepository, OpenAQClient openAQClient) {
        this.locationRepository = locationRepository;
        this.openAQClient = openAQClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) {
            log.info("Location bootstrap is disabled");
            return;
        }

        long existingCount = 0L;
        try {
            existingCount = locationRepository.findAll(PageRequest.of(0, 1)).getTotalElements();
        } catch (Exception e) {
            log.debug("Could not determine existing location count via paging; proceeding.", e);
        }

        if (existingCount > 0 && !forceRefresh) {
            log.info("Database already contains {} locations, skipping bootstrap", existingCount);
            return;
        }

        if (forceRefresh && existingCount > 0) {
            log.info("Force refresh enabled, will update existing locations");
        }

        log.info("Starting location bootstrap from OpenAQ API v3 for city: {}", city);

        try {
            List<OpenAQLocation> openAQLocations = openAQClient.fetchLocationsByCity(city, locationLimit);

            if (openAQLocations.isEmpty()) {
                log.warn("No locations fetched from OpenAQ API");
                return;
            }

            List<Location> toSave = new ArrayList<>();
            int skipped = 0;
            int duplicates = 0;

            for (OpenAQLocation src : openAQLocations) {
                Location mapped = mapToLocation(src);
                if (mapped == null) {
                    skipped++;
                    continue;
                }
                if (!forceRefresh) {
                    Optional<Location> existing = locationRepository.findById(mapped.getId());
                    if (existing.isPresent()) {
                        duplicates++;
                        continue;
                    }
                }
                toSave.add(mapped);
            }

            List<Location> saved = new ArrayList<>();
            for (Location l : toSave) {
                try {
                    saved.add(locationRepository.save(l));
                } catch (Exception e) {
                    log.warn("Failed to save location {} - {}", l.getId().value(), l.getName(), e);
                }
            }

            if (!saved.isEmpty()) {
                log.info("Successfully bootstrapped {} locations (skipped: {}, duplicates: {})", saved.size(), skipped, duplicates);
                saved.stream().limit(5).forEach(loc ->
                    log.debug("Saved location: {} - {} ({}, {})",
                        loc.getId().value(),
                        loc.getName(),
                        loc.getCoordinates().latitude(),
                        loc.getCoordinates().longitude())
                );
            } else {
                log.info("No new locations to save (duplicates: {})", duplicates);
            }
        } catch (Exception e) {
            log.error("Failed to bootstrap locations", e);
        }
    }

    private Location mapToLocation(OpenAQLocation src) {
        try {
            if (src.coordinates() == null || src.coordinates().latitude() == null || src.coordinates().longitude() == null) {
                log.debug("Skipping location without coordinates: {}", src.name());
                return null;
            }

            String locationId = generateLocationId(src);
            String name = determineBestName(src);
            if (name == null || name.isBlank()) {
                log.debug("Skipping location without name");
                return null;
            }

            LocationType type = determineLocationType(src);
            Coordinates coords = new Coordinates(src.coordinates().latitude(), src.coordinates().longitude());

            return new Location(new LocationId(locationId), name, coords, type);
        } catch (Exception e) {
            log.warn("Failed to map OpenAQ location: {}", safe(src.name()), e);
            return null;
        }
    }

    private String generateLocationId(OpenAQLocation src) {
        if (src.id() != null) {
            return "openaq-" + src.id();
        }
        String base = src.locality() != null ? src.locality() : src.name();
        if (base != null) {
            return base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        }
        return "location-" + System.currentTimeMillis();
    }

    private String determineBestName(OpenAQLocation src) {
        if (src.locality() != null && !src.locality().isBlank()) return src.locality();
        if (src.name() != null && !src.name().isBlank()) return src.name();
        return src.city();
    }

    private LocationType determineLocationType(OpenAQLocation src) {
        // Map to existing enum set: URBAN, SUBURBAN, RURAL, PARK
        String providerName = (src.provider() != null && src.provider().name() != null)
            ? src.provider().name().toLowerCase(Locale.ROOT)
            : "";
        String locality = src.locality() != null ? src.locality().toLowerCase(Locale.ROOT) : "";
        String name = src.name() != null ? src.name().toLowerCase(Locale.ROOT) : "";

        if (locality.contains("park") || name.contains("park")) {
            return LocationType.PARK;
        }
        if (Boolean.TRUE.equals(src.isMobile())) {
            return LocationType.SUBURBAN;
        }
        if (providerName.contains("government") || providerName.contains("reference") || providerName.contains("embassy") || providerName.contains("consulate")) {
            return LocationType.URBAN;
        }
        if (providerName.contains("community") || providerName.contains("low-cost") || providerName.contains("purpleair") || providerName.contains("airnow")) {
            return LocationType.SUBURBAN;
        }
        return LocationType.URBAN;
    }

    private String safe(String s) {
        return s == null ? "<unknown>" : s;
    }
}
