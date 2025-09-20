package com.dhakarun.domain.airquality.service;

import com.dhakarun.domain.airquality.model.AQI;
import com.dhakarun.domain.airquality.model.PollutantConcentration;
import java.util.List;

public class AQICalculator {

    public AQI calculate(List<PollutantConcentration> concentrations) {
        int score = concentrations.stream()
            .mapToInt(c -> (int) Math.round(c.microgramsPerCubicMeter()))
            .max()
            .orElse(0);
        return new AQI(score);
    }
}
