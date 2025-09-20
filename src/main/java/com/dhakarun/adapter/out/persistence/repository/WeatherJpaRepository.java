package com.dhakarun.adapter.out.persistence.repository;

import com.dhakarun.adapter.out.persistence.entity.WeatherEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherJpaRepository extends JpaRepository<WeatherEntity, Long> {

    Optional<WeatherEntity> findTopByLocationIdOrderByMeasuredAtDesc(String locationId);
}
