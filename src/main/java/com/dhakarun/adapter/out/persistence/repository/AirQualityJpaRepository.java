package com.dhakarun.adapter.out.persistence.repository;

import com.dhakarun.adapter.out.persistence.entity.AirQualityEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirQualityJpaRepository extends JpaRepository<AirQualityEntity, Long> {

    Optional<AirQualityEntity> findTopByLocationIdOrderByMeasuredAtDesc(String locationId);
}
