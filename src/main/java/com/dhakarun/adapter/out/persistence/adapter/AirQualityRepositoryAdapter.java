package com.dhakarun.adapter.out.persistence.adapter;

import com.dhakarun.adapter.out.persistence.mapper.EntityMapper;
import com.dhakarun.adapter.out.persistence.repository.AirQualityJpaRepository;
import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.airquality.repository.AirQualityRepository;
import com.dhakarun.domain.location.model.LocationId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AirQualityRepositoryAdapter implements AirQualityRepository {

    private final AirQualityJpaRepository repository;
    private final EntityMapper mapper;

    public AirQualityRepositoryAdapter(AirQualityJpaRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AirQualityReading> findLatestByLocation(LocationId locationId) {
        return repository
            .findTopByLocationIdOrderByMeasuredAtDesc(locationId.value())
            .map(mapper::toDomain);
    }

    @Override
    public AirQualityReading save(AirQualityReading reading) {
        var saved = repository.save(mapper.toEntity(reading));
        return mapper.toDomain(saved);
    }
}
