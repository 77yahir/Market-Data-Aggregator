package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.Random;

@Component
@ConditionalOnProperty(
        prefix = "sources.outlier",
        name = "enabled",
        havingValue = "true"
)
public class OutlierPriceSource implements PriceSource {
    private final String name = "OutlierPriceSource";
    private final Clock clock;

    public OutlierPriceSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<PriceTick> getLatestTick(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        Random random = new Random();
        PriceTick priceTick = new PriceTick(symbol.toUpperCase(), random.nextDouble(40000,60000), clock.instant(), name);
        return Optional.of(priceTick);
    }

    @Override
    public String name() {
        return name;
    }
}
