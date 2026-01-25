package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.domain.PriceTick;
import com.yahir.marketdataaggregator.sources.NormalPriceSource;
import com.yahir.marketdataaggregator.sources.OutlierPriceSource;
import com.yahir.marketdataaggregator.sources.PriceSource;
import com.yahir.marketdataaggregator.sources.StalePriceSource;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class PriceSourceTests {

    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

    @Test
    public void testNormalPriceSource() {
        PriceSource priceSource = new NormalPriceSource(fixedClock);
        Optional<PriceTick> normalTick = priceSource.getLatestTick("BTCUSD");

        double lowerBound = 0.0;
        double upperBound = 30000.0;
        assertTrue(normalTick.isPresent());
        assertFalse(normalTick.get().getTimeStamp().isAfter(fixedClock.instant()));
        double price = normalTick.get().getPrice();

        assertEquals("BTCUSD", normalTick.get().getSymbol());
        assertEquals("normalPriceSource", normalTick.get().getSource());
        assertThat(price).isBetween(lowerBound, upperBound);
    }

    @Test
    public void testOutlierPriceSource() {
        PriceSource priceSource = new OutlierPriceSource(fixedClock);
        Optional<PriceTick> outlierTick = priceSource.getLatestTick("BTCUSD");

        double lowerBound = 0.0;
        double upperBound = 30000.0;
        assertTrue(outlierTick.isPresent());
        double price = outlierTick.get().getPrice();

        assertEquals("BTCUSD", outlierTick.get().getSymbol());
        assertEquals("OutlierPriceSource", outlierTick.get().getSource());
        assertTrue(price < lowerBound || price > upperBound);
    }

    @Test
    public void testStalePriceSource() {
        PriceSource priceSource = new StalePriceSource(fixedClock);
        Optional<PriceTick> staleTick = priceSource.getLatestTick("BTCUSD");


        assertTrue(staleTick.isPresent());
        assertEquals("BTCUSD", staleTick.get().getSymbol());
        assertEquals("StalePriceSource", staleTick.get().getSource());
        assertTrue(staleTick.get().getTimeStamp().isBefore(fixedClock.instant().minus(Duration.ofSeconds(60))));
    }
}

