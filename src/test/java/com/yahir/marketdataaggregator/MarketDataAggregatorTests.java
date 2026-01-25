package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.error.Mark;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class MarketDataAggregatorTests {

    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

    @Test
    public void testNewestTickIsBest() {
        PriceTick priceTick = new PriceTick("BTCUSD", 2000, fixedClock.instant(), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);

        marketDataAggregator.ingest(priceTick);
        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(2000, marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void testNewestTickWins() {
        PriceTick priceTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick latePriceTick = new PriceTick("BTCUSD", 30000, fixedClock.instant().plusSeconds(36000), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);
        AggregatedPrice bestPrice = new AggregatedPrice(latePriceTick);

        marketDataAggregator.ingest(priceTick);
        marketDataAggregator.ingest(latePriceTick);
        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(bestPrice.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void rejectTicksThatAreStale() {
        PriceTick priceTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick staleTick = new PriceTick("BTCUSD", 24000, fixedClock.instant().minusSeconds(600), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);
        marketDataAggregator.ingest(priceTick);
        marketDataAggregator.ingest(staleTick);
        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(priceTick.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void testRejectTicksThatAreOutliers() {
        PriceTick initialPriceTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick invalidSpikeTick = new PriceTick("BTCUSD", 50000, fixedClock.instant().plusSeconds(5000), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);
        AggregatedPrice bestPrice = new AggregatedPrice(initialPriceTick);

        marketDataAggregator.ingest(initialPriceTick);
        marketDataAggregator.ingest(invalidSpikeTick);

        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(bestPrice.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void testAcceptsTicksThatHaveValidChange() {
        PriceTick initialPriceTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick validChangeTick = new PriceTick("BTCUSD", 25500, fixedClock.instant().plusSeconds(5), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);
        AggregatedPrice bestPrice = new AggregatedPrice(validChangeTick);

        marketDataAggregator.ingest(initialPriceTick);
        marketDataAggregator.ingest(validChangeTick);

        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(bestPrice.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void testTicksWithSameTimeStampTieBreaker() {
        PriceTick tick1 = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "ATierSource");
        PriceTick tick2 = new PriceTick("BTCUSD", 23500, fixedClock.instant(), "BetterSource");
        PriceTick tick3 = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "WeakSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);
        marketDataAggregator.ingest(tick1);
        marketDataAggregator.ingest(tick2);
        marketDataAggregator.ingest(tick3);
        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(tick1.getSource(), marketDataAggregator.getBest("BTCUSD").get().getSource());
    }



}
