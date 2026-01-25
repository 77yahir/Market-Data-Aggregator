package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.PriceTick;
import com.yahir.marketdataaggregator.service.MarketDataService;
import com.yahir.marketdataaggregator.sources.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarketDataServiceTests {
    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

    @Test
    public void pollCallsAllSources() {
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);
        List<PriceSource> sources = new ArrayList<>();
        sources.add(new NormalPriceSource(fixedClock));
        sources.add(new OutlierPriceSource(fixedClock));
        sources.add(new StalePriceSource(fixedClock));
        MarketDataService service = new MarketDataService(sources, aggregator);
        service.pollOnce("BTCUSD");
        assertTrue(service.getBest("BTCUSD").isPresent());
        assertEquals("NormalPriceSource", service.getBest("BTCUSD").get().getSource());
    }

    @Test
    public void pollHandlesEmptySources() {
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);
        List<PriceSource> sources = new ArrayList<>();
        sources.add(new NormalPriceSource(fixedClock));
        sources.add(new EmptyPriceSource(fixedClock));
        MarketDataService service = new MarketDataService(sources, aggregator);
        service.pollOnce("BTCUSD");
        assertTrue(service.getBest("BTCUSD").isPresent());
        assertEquals("NormalPriceSource", service.getBest("BTCUSD").get().getSource());
    }

    @Test
    public void getBestDelegates() {
        MarketDataAggregator aggregator = new MarketDataAggregator(fixedClock);
        PriceTick pt = new PriceTick("BTCUSD", 20500, fixedClock.instant().plusSeconds(100), "TestSource");
        aggregator.ingest(pt);
        List<PriceSource> sources = new ArrayList<>();
        MarketDataService service = new MarketDataService(sources, aggregator);

        assertTrue(service.getBest("BTCUSD").isPresent());
        assertEquals("TestSource", service.getBest("BTCUSD").get().getSource());
    }
}
