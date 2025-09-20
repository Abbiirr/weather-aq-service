package com.dhakarun.domain.location.service;

import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.repository.LocationRepository;

import java.util.Optional;

public class LocationDomainService {

    private final LocationRepository repository;

    public LocationDomainService(LocationRepository repository) {
        this.repository = repository;
    }

    public Optional<Location> find(LocationId id) {
        return repository.findById(id);
    }


    public Location register(Location location) {
        return repository.save(location);
    }
}
