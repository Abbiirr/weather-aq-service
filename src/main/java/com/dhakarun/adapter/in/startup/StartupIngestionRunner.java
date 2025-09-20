package com.dhakarun.adapter.in.startup;

import com.dhakarun.application.service.DataIngestionService;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class StartupIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupIngestionRunner.class);

    private final DataIngestionService dataIngestionService;
    private final LocationRepository locationRepository;

    public StartupIngestionRunner(DataIngestionService dataIngestionService, LocationRepository locationRepository) {
        this.dataIngestionService = dataIngestionService;
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running startup data ingestion for all locations");
        processAllLocations(500);
        log.info("Startup data ingestion completed");
    }

    private void processAllLocations(int pageSize) {
        int page = 0;
        Page<Location> result;
        do {
            Pageable pageable = PageRequest.of(page, pageSize);
            result = locationRepository.findAll(pageable);
            result.getContent().forEach(loc -> dataIngestionService.refreshFromDataSources(loc.getId().value()));
            page++;
        } while (!result.isEmpty() && result.hasNext());
    }
}
