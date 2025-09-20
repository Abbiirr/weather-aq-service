package com.dhakarun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DhakaRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(DhakaRunApplication.class, args);
    }
}
