package com.dhakarun.domain.location.repository;

import com.dhakarun.domain.location.model.Location;
import com.dhakarun.domain.location.model.LocationId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationRepository {

    Optional<Location> findById(LocationId id);

    Location save(Location location);

    Page<Location> findAll(Pageable pageable);
}
