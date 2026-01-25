package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.abs;

@Component
public class MarketDataAggregator {
    private final Clock clock;
    private final Duration staleThreshold = Duration.ofSeconds(60);
    private final double outlierPct = 0.25;
    private final Map<String, AggregatedPrice> bestBySymbol = new HashMap<>();

    public MarketDataAggregator(Clock clock) {
        this.clock = clock;
    }

    public void ingest(PriceTick tick) {
        if (tick.isEmpty()) {
            return;
        }

        Instant now = clock.instant();
        if (isStale(tick, now)) {
            return;
        }

        AggregatedPrice current = bestBySymbol.get(tick.getSymbol());

        if (current == null) {
            bestBySymbol.put(tick.getSymbol(), new AggregatedPrice(tick));
            return;
        }
        if (isOutlier(tick, current)) {
            return;
        }
        if (tick.getTimeStamp().isAfter(current.getTimeStamp())) {
            bestBySymbol.put(tick.getSymbol(), new AggregatedPrice(tick));
            return;
        }

        if (tick.getTimeStamp().equals(current.getTimeStamp())) {
            if (tick.getSource().compareToIgnoreCase(current.getSource()) < 0) {
                bestBySymbol.put(tick.getSymbol(), new AggregatedPrice(tick));
            }
            return;
        }
    }

    public Optional<AggregatedPrice> getBest(String symbol) {
        if (bestBySymbol.containsKey(symbol) && bestBySymbol.get(symbol).getPrice() > 0) {
            return Optional.ofNullable(bestBySymbol.get(symbol));
        } else {
            return Optional.empty();
        }
    }

    public Map<String, AggregatedPrice> getAllBest() {
        return Collections.unmodifiableMap(bestBySymbol);
    }

    private boolean isStale(PriceTick priceTick, Instant now) {
        if (priceTick.getTimeStamp().isBefore(now.minus(staleThreshold))) {
            return true;
        }
        return false;
    }

    private boolean isOutlier(PriceTick priceTick, AggregatedPrice best) {
        if (best.getPrice() <= 0) {
            return false;
        }
        double percentage = (abs(priceTick.getPrice() - best.getPrice()) / best.getPrice());
        if (percentage > outlierPct) {
            return true;
        }
        return false;
    }

    private boolean shouldReplace(PriceTick priceTick, AggregatedPrice best) {
        if (priceTick.getTimeStamp().equals(best.getTimeStamp())) {
            return priceTick.getPrice() < best.getPrice();
        }
        return priceTick.getTimeStamp().isAfter(best.getTimeStamp());

    }
}
