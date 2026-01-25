package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MarketDataAggregatorTests {

    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

//    .ingest() tests

    /**
     * Tests that .ingest() rejects empty tick, getBest() returns Optional.Empty(), getAllBest() returns Empty HashMap
     */

    @Test
    public void rejectEmptyTick() {
        PriceTick priceTick = new PriceTick();
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);
        Map<String, AggregatedPrice> emptyMap = new HashMap<>();

        aggregator.ingest(priceTick);
        assertFalse(aggregator.getBest("BTCUSD").isPresent());
        assertEquals(emptyMap, aggregator.getAllBest());
        assertEquals(Optional.empty(), aggregator.getBest("BTCUSD"));
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
    public void acceptsNewTicksNotYetStored() {
        PriceTick btcTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick ethTick = new PriceTick("ETHUSD", 2000, fixedClock.instant(), "TestSource");
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);

        aggregator.ingest(btcTick);
        aggregator.ingest(ethTick);
        assertTrue(aggregator.getBest("BTCUSD").isPresent());
        assertTrue(aggregator.getBest("ETHUSD").isPresent());
        assertEquals(ethTick.getPrice(), aggregator.getBest("ETHUSD").get().getPrice());
        assertEquals(btcTick.getPrice(), aggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void allSymbolsGetStoredAsUpperCase() {
        PriceTick btcTick = new PriceTick("btcusd", 25000, fixedClock.instant(), "TestSource");
        PriceTick ethTick = new  PriceTick("ethusd", 2000, fixedClock.instant(), "TestSource");
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);

        aggregator.ingest(btcTick);
        aggregator.ingest(ethTick);
        assertTrue(aggregator.getBest("BTCUSD").isPresent());
        assertTrue(aggregator.getBest("ETHUSD").isPresent());
        assertEquals(btcTick.getSymbol(), aggregator.getBest("BTCUSD").get().getSymbol());
        assertEquals(ethTick.getSymbol(), aggregator.getBest("ETHUSD").get().getSymbol());

        assertTrue(aggregator.getBest("btcusd").isPresent());
        assertTrue(aggregator.getBest("ethusd").isPresent());
        assertEquals(btcTick.getSymbol(), aggregator.getBest("btcusd").get().getSymbol());
        assertEquals(ethTick.getSymbol(), aggregator.getBest("ethusd").get().getSymbol());
    }

    @Test
    public void rejectTicksThatAreOutliers() {
        PriceTick initialPriceTick = new PriceTick("BTCUSD", 25000, fixedClock.instant(), "TestSource");
        PriceTick invalidSpikeTick = new PriceTick("BTCUSD", 50000, fixedClock.instant().plusSeconds(5000), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);

        marketDataAggregator.ingest(initialPriceTick);
        marketDataAggregator.ingest(invalidSpikeTick);

        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(initialPriceTick.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void newestTickIsBest() {
        PriceTick priceTick = new PriceTick("BTCUSD", 2000, fixedClock.instant(), "TestSource");
        MarketDataAggregator marketDataAggregator = new MarketDataAggregator(fixedClock);

        marketDataAggregator.ingest(priceTick);

        assertTrue(marketDataAggregator.getBest("BTCUSD").isPresent());
        assertEquals(priceTick.getPrice(), marketDataAggregator.getBest("BTCUSD").get().getPrice());
    }

    @Test
    public void newestTickWins() {
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
    public void acceptsTicksThatHaveValidChange() {
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
    public void ticksWithSameTimeStampGetTieBroken() {
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

    @Test
    public void getBestOffNullOrEmptyReturnsEmpty() {
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);
        String testNull = null;

        assertEquals(Optional.empty(), aggregator.getBest(" "));
        assertEquals(Optional.empty(), aggregator.getBest(""));
        assertEquals(Optional.empty(), aggregator.getBest(testNull));
    }

}
