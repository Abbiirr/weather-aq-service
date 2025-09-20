package com.dhakarun.config;

import com.dhakarun.config.properties.OpenAQProperties;
import com.dhakarun.config.properties.OpenMeteoProperties;
import com.dhakarun.domain.running.service.RunConditionEvaluator;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OpenAQProperties.class, OpenMeteoProperties.class})
public class ApplicationConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RunConditionEvaluator runConditionEvaluator() {
        return new RunConditionEvaluator();
    }
}
