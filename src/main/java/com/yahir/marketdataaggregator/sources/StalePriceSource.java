package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

public class StalePriceSource implements PriceSource {
    private final String name = "StalePriceSource";
    private final Clock clock;

    public StalePriceSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<PriceTick> getLatestTick(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        Random random = new Random();
        PriceTick priceTick = new PriceTick(symbol, random.nextDouble(30000), clock.instant().minus(100, ChronoUnit.MINUTES), name);
        return Optional.of(priceTick);
    }

    @Override
    public String name() {
        return name;
    }
}
