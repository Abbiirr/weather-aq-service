package com.dhakarun.application.port.out;

import com.dhakarun.application.shared.PageQuery;
import com.dhakarun.application.shared.PageResult;
import com.dhakarun.domain.location.model.Location;

public interface LocationReadPort {

    PageResult<Location> fetchPage(PageQuery query);
}
