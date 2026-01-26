package com.yahir.marketdataaggregator.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class Config {

    @Bean
    public Clock fixedClock() {
        return Clock.systemUTC();
    }
}
