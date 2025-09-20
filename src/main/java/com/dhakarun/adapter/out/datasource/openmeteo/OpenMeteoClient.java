package com.dhakarun.adapter.out.datasource.openmeteo;

import com.dhakarun.adapter.out.datasource.openmeteo.dto.OpenMeteoResponse;
import com.dhakarun.domain.location.model.LocationId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    public Optional<OpenMeteoResponse> fetchLatest(LocationId locationId) {
        log.debug("Fetching weather for {}", locationId.value());
        return Optional.empty();
    }
}
