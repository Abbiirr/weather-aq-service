package com.dhakarun.application.port.in;

import com.dhakarun.application.shared.PageQuery;
import com.dhakarun.application.shared.PageResult;

public interface BrowseLocationsUseCase {

    PageResult<GetLocationSummaryUseCase.LocationSummaryView> browse(PageQuery query);
}
