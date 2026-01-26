package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.abs;

@Component
public class MarketDataAggregator {
    private final Clock clock;
    private final Duration staleThreshold = Duration.ofSeconds(60);
    private final double outlierPct = 0.25;
    private final Map<String, AggregatedPrice> bestBySymbol = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(MarketDataAggregator.class);

    public MarketDataAggregator(Clock clock) {
        this.clock = clock;
    }

    public void ingest(PriceTick tick) {
        if (tick.isEmpty()) {
            log.debug("Ignored empty tick for symbol={} source={}", tick.getSymbol(), tick.getSource());
            return;
        }

        Instant now = clock.instant();
        if (isStale(tick, now)) {
            log.warn("Rejected stale tick for symbol={} ts={} now={} source={}",
                    tick.getSymbol(), tick.getTimeStamp(), now, tick.getSource());
            return;
        }

        String tickSymbol = tick.getSymbol();
        AggregatedPrice current = bestBySymbol.get(tickSymbol);

        if (current == null) {
            bestBySymbol.put(tickSymbol, new AggregatedPrice(tick));
            log.info("Accepted new best price for symbol={} price={} source={} ts={}",
                    tickSymbol, tick.getPrice(), tick.getSource(), tick.getTimeStamp());
            return;
        }
        if (isOutlier(tick, current)) {
            log.warn("Rejected outlier tick symbol={} price={} currentPrice={} pctThreshold={} source={}",
                    tickSymbol, tick.getPrice(), current.getPrice(), outlierPct, tick.getSource());
            return;
        }
        if (tick.getTimeStamp().isAfter(current.getTimeStamp())) {
            bestBySymbol.put(tickSymbol, new AggregatedPrice(tick));
            log.info("Replaced best price for symbol=[} oldTs={} newTs={} newPrice={} source={}",
                    tickSymbol, tick.getTimeStamp(), current.getTimeStamp(), tick.getSource());
            return;
        }

        if (tick.getTimeStamp().equals(current.getTimeStamp())) {
            if (shouldReplace(tick, current)) {
                bestBySymbol.put(tickSymbol, new AggregatedPrice(tick));
                log.info("Tie-break replaced best for symbol={} timeStamp={} newSource={}",
                        tickSymbol, tick.getTimeStamp(), tick.getSource());
                return;
            }
            log.debug("Tie-break dropped tick for symbol={} source={} (kept {})",
                    tickSymbol, tick.getSource(), tick.getTimeStamp());
            return;
        }
        log.debug("Tick ignored for symbol={} (older than current) tickTs={} currentTs={}",
                tickSymbol, tick.getTimeStamp(), current.getTimeStamp());
    }

    public Optional<AggregatedPrice> getBest(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            log.error("symbol is empty");
            return Optional.empty();
        }
        String tickSymbol = symbol.trim().toUpperCase();
        if (bestBySymbol.containsKey(tickSymbol) && bestBySymbol.get(tickSymbol).getPrice() > 0) {
            return Optional.ofNullable(bestBySymbol.get(tickSymbol));
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

        AggregatedPrice newTick = new AggregatedPrice(priceTick);
        List<AggregatedPrice> prices = Arrays.asList(newTick, best);

        prices.sort(Comparator.comparing((AggregatedPrice n) -> n.getSource().toUpperCase())
                .thenComparing(AggregatedPrice::getPrice));

        return prices.getFirst().equals(newTick);

    }
}
