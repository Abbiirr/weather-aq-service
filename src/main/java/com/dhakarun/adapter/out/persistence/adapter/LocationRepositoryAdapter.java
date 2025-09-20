package com.dhakarun.adapter.out.persistence.adapter;

import com.dhakarun.adapter.out.persistence.mapper.EntityMapper;
import com.dhakarun.adapter.out.persistence.repository.LocationJpaRepository;
import com.dhakarun.application.port.out.LocationReadPort;
import com.dhakarun.application.shared.PageQuery;
import com.dhakarun.application.shared.PageResult;
import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.location.repository.LocationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class LocationRepositoryAdapter implements LocationRepository, LocationReadPort {

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
    public Location save(Location location) {
        var saved = repository.save(mapper.toEntity(location));
        return mapper.toDomain(saved);
    }

    @Override
    public PageResult<Location> fetchPage(PageQuery query) {
        var pageRequest = PageRequest.of(query.page(), query.size());
        var page = repository.findAll(pageRequest);
        List<Location> content = page.getContent().stream()
            .map(mapper::toDomain)
            .toList();
        return new PageResult<>(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.hasNext(),
            content
        );
    }
}
