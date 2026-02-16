package com.yahir.marketdataaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MarketDataAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketDataAggregatorApplication.class, args);
	}

}
