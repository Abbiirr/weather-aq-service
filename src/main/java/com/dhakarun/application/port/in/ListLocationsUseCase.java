package com.dhakarun.application.port.in;

import com.dhakarun.application.shared.PageQuery;
import com.dhakarun.application.shared.PageResult;
import com.dhakarun.domain.location.model.Location;

public interface ListLocationsUseCase {

    PageResult<Location> list(PageQuery query);
}
