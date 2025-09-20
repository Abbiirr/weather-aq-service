package com.dhakarun.adapter.out.persistence.adapter;

import com.dhakarun.adapter.out.persistence.mapper.EntityMapper;
import com.dhakarun.adapter.out.persistence.repository.LocationJpaRepository;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.repository.LocationRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class LocationRepositoryAdapter implements LocationRepository {

    private final LocationJpaRepository repository;
    private final EntityMapper mapper;

    public LocationRepositoryAdapter(LocationJpaRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Location> findById(LocationId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Page<Location> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Location save(Location location) {
        var saved = repository.save(mapper.toEntity(location));
        return mapper.toDomain(saved);
    }
}
