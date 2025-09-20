package com.dhakarun.adapter.out.persistence.adapter;

import com.dhakarun.adapter.out.persistence.mapper.EntityMapper;
import com.dhakarun.adapter.out.persistence.repository.WeatherJpaRepository;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.repository.WeatherRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class WeatherRepositoryAdapter implements WeatherRepository {

    private final WeatherJpaRepository repository;
    private final EntityMapper mapper;

    public WeatherRepositoryAdapter(WeatherJpaRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<WeatherReading> findLatestByLocation(LocationId locationId) {
        return repository
            .findTopByLocationIdOrderByMeasuredAtDesc(locationId.value())
            .map(mapper::toDomain);
    }

    @Override
    public WeatherReading save(WeatherReading reading) {
        var saved = repository.save(mapper.toEntity(reading));
        return mapper.toDomain(saved);
    }
}
