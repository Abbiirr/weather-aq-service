package com.dhakarun.adapter.in.web;

import com.dhakarun.adapter.in.web.dto.LocationDetailsResponse;
import com.dhakarun.adapter.in.web.dto.LocationSummaryResponse;
import com.dhakarun.adapter.in.web.dto.PageResponse;
import com.dhakarun.adapter.in.web.mapper.LocationDtoMapper;
import com.dhakarun.application.port.in.BrowseLocationsUseCase;
import com.dhakarun.application.port.in.GetLocationDetailsUseCase;
import com.dhakarun.application.port.in.GetLocationSummaryUseCase;
import com.dhakarun.application.port.in.RefreshLocationDataUseCase;
import com.dhakarun.application.shared.PageQuery;
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

    private final GetLocationSummaryUseCase getLocationSummaryUseCase;
    private final GetLocationDetailsUseCase getLocationDetailsUseCase;
    private final BrowseLocationsUseCase browseLocationsUseCase;
    private final RefreshLocationDataUseCase refreshLocationDataUseCase;
    private final LocationDtoMapper mapper;

    public LocationController(
        GetLocationSummaryUseCase getLocationSummaryUseCase,
        GetLocationDetailsUseCase getLocationDetailsUseCase,
        BrowseLocationsUseCase browseLocationsUseCase,
        RefreshLocationDataUseCase refreshLocationDataUseCase,
        LocationDtoMapper mapper
    ) {
        this.getLocationSummaryUseCase = getLocationSummaryUseCase;
        this.getLocationDetailsUseCase = getLocationDetailsUseCase;
        this.browseLocationsUseCase = browseLocationsUseCase;
        this.refreshLocationDataUseCase = refreshLocationDataUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public PageResponse<LocationSummaryResponse> getAllSummaries(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var summaries = browseLocationsUseCase.browse(new PageQuery(page, size));
        var content = summaries.content().stream()
            .map(mapper::toSummary)
            .toList();

        return new PageResponse<>(
            summaries.page(),
            summaries.size(),
            summaries.totalElements(),
            content
        );
    }

    @GetMapping("/{locationId}/summary")
    public LocationSummaryResponse getSummary(@PathVariable String locationId) {
        try {
            var summary = getLocationSummaryUseCase.getSummary(new LocationId(locationId));
            return mapper.toSummary(summary);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{locationId}")
    public LocationDetailsResponse getDetails(@PathVariable String locationId) {
        try {
            var details = getLocationDetailsUseCase.getDetails(new LocationId(locationId));
            return mapper.toDetails(details);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{locationId}/refresh")
    public void refresh(@PathVariable String locationId) {
        log.debug("Manual refresh requested for location {}", locationId);
        refreshLocationDataUseCase.refresh(locationId);
    }
}
