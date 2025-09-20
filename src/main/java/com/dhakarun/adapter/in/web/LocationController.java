package com.dhakarun.adapter.in.web;

import com.dhakarun.adapter.in.web.dto.LocationDetailsResponse;
import com.dhakarun.adapter.in.web.dto.LocationSummaryResponse;
import com.dhakarun.adapter.in.web.dto.PageResponse;
import com.dhakarun.adapter.in.web.mapper.LocationDtoMapper;
import com.dhakarun.application.service.DataIngestionService;
import com.dhakarun.application.service.LocationQueryService;
import com.dhakarun.domain.location.model.LocationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    private final LocationQueryService locationQueryService;
    private final LocationDtoMapper mapper;
    private final DataIngestionService dataIngestionService;

    public LocationController(
        LocationQueryService locationQueryService,
        LocationDtoMapper mapper,
        DataIngestionService dataIngestionService
    ) {
        this.locationQueryService = locationQueryService;
        this.mapper = mapper;
        this.dataIngestionService = dataIngestionService;
    }

    @GetMapping
    public PageResponse<LocationSummaryResponse> getAllSummaries(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var summaries = locationQueryService.getAllSummaries(page, size);
        var content = summaries.getContent().stream()
            .map(mapper::toSummary)
            .toList();

        return new PageResponse<>(
            summaries.getNumber(),
            summaries.getSize(),
            summaries.getTotalElements(),
            content
        );
    }

    @GetMapping("/{locationId}/summary")
    public LocationSummaryResponse getSummary(@PathVariable String locationId) {
        try {
            var summary = locationQueryService.getSummary(new LocationId(locationId));
            return mapper.toSummary(summary);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{locationId}")
    public LocationDetailsResponse getDetails(@PathVariable String locationId) {
        try {
            var details = locationQueryService.getDetails(new LocationId(locationId));
            return mapper.toDetails(details);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{locationId}/refresh")
    public void refresh(@PathVariable String locationId) {
        dataIngestionService.refreshFromDataSources(new LocationId(locationId));
    }
}
