package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;

import java.time.Clock;
import java.util.Optional;

public class EmptyPriceSource implements PriceSource {
    private final String name = "EmptyPriceSource";
    private final Clock clock;

    public EmptyPriceSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<PriceTick> getLatestTick(String symbol) {
        return Optional.empty();
    }

    @Override
    public String name() {
        return "";
    }
}
