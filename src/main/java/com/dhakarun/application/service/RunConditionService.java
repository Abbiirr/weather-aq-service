package com.dhakarun.application.service;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.airquality.repository.AirQualityRepository;
import com.dhakarun.domain.location.model.LocationId;
import com.dhakarun.domain.running.model.RunCondition;
import com.dhakarun.domain.running.service.RunConditionEvaluator;
import com.dhakarun.domain.weather.model.WeatherReading;
import com.dhakarun.domain.weather.repository.WeatherRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RunConditionService {

    private final AirQualityRepository airQualityRepository;
    private final WeatherRepository weatherRepository;
    private final RunConditionEvaluator evaluator;

    public RunConditionService(
        AirQualityRepository airQualityRepository,
        WeatherRepository weatherRepository,
        RunConditionEvaluator evaluator
    ) {
        this.airQualityRepository = airQualityRepository;
        this.weatherRepository = weatherRepository;
        this.evaluator = evaluator;
    }

    public Optional<RunCondition> evaluate(LocationId locationId) {
        AirQualityReading airQuality = airQualityRepository.findLatestByLocation(locationId).orElse(null);
        WeatherReading weather = weatherRepository.findLatestByLocation(locationId).orElse(null);
        if (airQuality == null && weather == null) {
            return Optional.empty();
        }
        return Optional.of(evaluator.evaluate(airQuality, weather));
    }
}
