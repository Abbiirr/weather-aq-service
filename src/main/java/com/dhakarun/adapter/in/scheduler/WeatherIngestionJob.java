package com.dhakarun.adapter.in.scheduler;

import com.dhakarun.application.port.in.ListLocationsUseCase;
import com.dhakarun.application.port.in.RefreshLocationDataUseCase;
import com.dhakarun.application.shared.PageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherIngestionJob {

    private static final Logger log = LoggerFactory.getLogger(WeatherIngestionJob.class);
    private final RefreshLocationDataUseCase refreshLocationDataUseCase;
    private final ListLocationsUseCase listLocationsUseCase;

    public WeatherIngestionJob(
        RefreshLocationDataUseCase refreshLocationDataUseCase,
        ListLocationsUseCase listLocationsUseCase
    ) {
        this.refreshLocationDataUseCase = refreshLocationDataUseCase;
        this.listLocationsUseCase = listLocationsUseCase;
    }

    @Scheduled(cron = "${app.scheduler.weather.cron}")
    public void ingest() {
        log.info("Running weather ingestion job");
        processAllLocations(500);
    }

    private void processAllLocations(int pageSize) {
        int page = 0;
        boolean hasNext;
        do {
            var pageResult = listLocationsUseCase.list(new PageQuery(page, pageSize));
            pageResult.content().forEach(location ->
                refreshLocationDataUseCase.refresh(location.getId())
            );
            hasNext = pageResult.hasNext();
            page++;
        } while (hasNext);
    }
}
