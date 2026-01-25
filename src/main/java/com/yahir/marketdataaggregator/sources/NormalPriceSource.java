package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.Random;

@Component
public class NormalPriceSource implements PriceSource {
    private final String name = "NormalPriceSource";
    private final Clock clock;

    public NormalPriceSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<PriceTick> getLatestTick(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        Random random = new Random();
        PriceTick priceTick = new PriceTick(symbol.toUpperCase(), random.nextDouble(30000.00), clock.instant(), name);
        return Optional.of(priceTick);
    }

    @Override
    public String name() {
        return name;
    }
}
