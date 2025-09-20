package com.dhakarun.adapter.in.startup;

import com.dhakarun.application.port.in.ListLocationsUseCase;
import com.dhakarun.application.port.in.RefreshLocationDataUseCase;
import com.dhakarun.application.shared.PageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class StartupIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupIngestionRunner.class);

    private final RefreshLocationDataUseCase refreshLocationDataUseCase;
    private final ListLocationsUseCase listLocationsUseCase;

    public StartupIngestionRunner(
        RefreshLocationDataUseCase refreshLocationDataUseCase,
        ListLocationsUseCase listLocationsUseCase
    ) {
        this.refreshLocationDataUseCase = refreshLocationDataUseCase;
        this.listLocationsUseCase = listLocationsUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running startup data ingestion for all locations");
        processAllLocations(500);
        log.info("Startup data ingestion completed");
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
