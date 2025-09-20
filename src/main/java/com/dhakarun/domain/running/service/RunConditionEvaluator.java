package com.dhakarun.domain.running.service;

import com.dhakarun.domain.airquality.model.AirQualityReading;
import com.dhakarun.domain.running.model.HealthRisk;
import com.dhakarun.domain.running.model.RunCondition;
import com.dhakarun.domain.running.model.RunVerdict;
import com.dhakarun.domain.weather.model.WeatherReading;

public class RunConditionEvaluator {

    public RunCondition evaluate(AirQualityReading airQuality, WeatherReading weather) {
        if (airQuality == null && weather == null) {
            throw new IllegalArgumentException("At least one reading is required");
        }

        RunVerdict verdict = RunVerdict.IDEAL;
        String message = "Great conditions for running.";

        if (airQuality != null && airQuality.getAqi().value() > 100) {
            verdict = RunVerdict.HAZARDOUS;
            message = "Air quality is not suitable for outdoor runs.";
        }

        if (weather != null && weather.getTemperature().valueCelsius() > 34) {
            verdict = RunVerdict.ACCEPTABLE;
            message = "High temperature detected, stay hydrated.";
        }

        if (weather != null && weather.getHumidity().percentage() > 85) {
            verdict = RunVerdict.ACCEPTABLE;
            message = "High humidity may cause discomfort.";
        }

        var locationId = airQuality != null ? airQuality.getLocationId() : weather.getLocationId();
        return new RunCondition(locationId, verdict, new HealthRisk(message));
    }
}

