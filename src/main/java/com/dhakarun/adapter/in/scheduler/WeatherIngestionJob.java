package com.dhakarun.adapter.in.scheduler;

import com.dhakarun.application.service.DataIngestionService;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.repository.LocationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherIngestionJob {

    private static final Logger log = LoggerFactory.getLogger(WeatherIngestionJob.class);
    private final DataIngestionService dataIngestionService;
    private final LocationRepository locationRepository;

    public WeatherIngestionJob(DataIngestionService dataIngestionService, LocationRepository locationRepository) {
        this.dataIngestionService = dataIngestionService;
        this.locationRepository = locationRepository;
    }

    @Scheduled(cron = "${app.scheduler.weather.cron}")
    public void ingest() {
        log.info("Running weather ingestion job");
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
