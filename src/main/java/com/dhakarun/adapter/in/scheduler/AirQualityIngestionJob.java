package com.dhakarun.adapter.in.scheduler;

import com.dhakarun.application.service.DataIngestionService;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AirQualityIngestionJob {

    private static final Logger log = LoggerFactory.getLogger(AirQualityIngestionJob.class);
    private final DataIngestionService dataIngestionService;
    private final LocationRepository locationRepository;

    public AirQualityIngestionJob(DataIngestionService dataIngestionService, LocationRepository locationRepository) {
        this.dataIngestionService = dataIngestionService;
        this.locationRepository = locationRepository;
    }

    @Scheduled(cron = "${app.scheduler.air-quality.cron}")
    public void ingest() {
        log.info("Running air quality ingestion job");
        processAllLocations(500);
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
