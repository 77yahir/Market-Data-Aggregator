package com.yahir.marketdataaggregator;


import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.MarketDataAggregator;
import com.yahir.marketdataaggregator.domain.PriceTick;
import com.yahir.marketdataaggregator.service.MarketDataService;
import com.yahir.marketdataaggregator.sources.PriceSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarketDataServiceMockitoTests {
    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

    @Mock
    PriceSource emptyPriceSource;

    @Mock
    PriceSource normalPriceSource;

    @Mock
    PriceSource outlierPriceSource;

    @Mock
    MarketDataAggregator marketDataAggregator;

    MarketDataService service;

    @BeforeEach
    public void setup(){
        List<PriceSource> priceSources = new ArrayList<>(Arrays.asList(emptyPriceSource, normalPriceSource, outlierPriceSource));
        service = new MarketDataService(priceSources, marketDataAggregator);
    }

    @Test
    public void allPriceSourcesGetChecked(){
        PriceTick pt = new PriceTick("BTCUSD", 35000, fixedClock.instant(), "normalPriceSource");

        when(emptyPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());
        when(normalPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.of(pt));
        when(outlierPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());

        service.pollOnce("BTCUSD");

        verify(emptyPriceSource).getLatestTick("BTCUSD");
        verify(normalPriceSource).getLatestTick("BTCUSD");
        verify(outlierPriceSource).getLatestTick("BTCUSD");
        verify(marketDataAggregator).ingest(pt);
        verifyNoMoreInteractions(marketDataAggregator);
    }

    @Test
    public void noInteractionWhenPriceSourcesAreEmpty() {
        when(emptyPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());
        when(normalPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());
        when(outlierPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());

        service.pollOnce("BTCUSD");

        verify(emptyPriceSource).getLatestTick("BTCUSD");
        verify(normalPriceSource).getLatestTick("BTCUSD");
        verify(outlierPriceSource).getLatestTick("BTCUSD");
        verifyNoInteractions(marketDataAggregator);
    }

    @Test
    public void pollOnce_forwardsAllTicksFromMultipleSources() {
        PriceTick pt1 = new PriceTick("BTCUSD", 35000, fixedClock.instant(), "normalPriceSource");
        PriceTick pt2 = new PriceTick("BTCUSD", 70000, fixedClock.instant(), "outlierPriceSource");

        when(emptyPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.empty());
        when(normalPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.of(pt1));
        when(outlierPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.of(pt2));

        service.pollOnce("BTCUSD");

        verify(emptyPriceSource).getLatestTick("BTCUSD");
        verify(normalPriceSource).getLatestTick("BTCUSD");
        verify(outlierPriceSource).getLatestTick("BTCUSD");
        verify(marketDataAggregator).ingest(pt1);
        verify(marketDataAggregator).ingest(pt2);
        verifyNoMoreInteractions(marketDataAggregator);
    }

    @Test
    public void handlesNoSourcesList() {
        List<PriceSource> priceSources = new ArrayList<>();

        MarketDataService service = new MarketDataService(priceSources, marketDataAggregator);

        service.pollOnce("BTCUSD");
        verifyNoInteractions(marketDataAggregator);
    }

    @Test
    public void getBest_callsMarketDataAggregatorAndReturns() {
        PriceTick pt1 = new PriceTick("BTCUSD", 35000, fixedClock.instant(), "normalPriceSource");
        AggregatedPrice aggregatedPrice = new AggregatedPrice(pt1);

        when(marketDataAggregator.getBest("BTCUSD")).thenReturn(Optional.of(aggregatedPrice));

        Optional<AggregatedPrice> result = service.getBest("BTCUSD");

        assert result.isPresent();
        assert result.get().equals(aggregatedPrice);

        verify(marketDataAggregator).getBest("BTCUSD");
        verifyNoMoreInteractions(marketDataAggregator);
    }

    @Test
    public void getAllBest_callsMarketDataAggregatorAndReturns() {
        PriceTick pt1 = new PriceTick("BTCUSD", 35000, fixedClock.instant(), "normalPriceSource");
        PriceTick pt2 = new PriceTick("ETHUSD", 3000, fixedClock.instant(), "normalPriceSource");

        when(normalPriceSource.getLatestTick("BTCUSD")).thenReturn(Optional.of(pt1));
        when(normalPriceSource.getLatestTick("ETHUSD")).thenReturn(Optional.of(pt2));

        service.pollOnce("BTCUSD");
        service.pollOnce("ETHUSD");
        marketDataAggregator.getAllBest();

        verify(normalPriceSource).getLatestTick("BTCUSD");
        verify(normalPriceSource).getLatestTick("ETHUSD");
        verify(marketDataAggregator).ingest(pt1);
        verify(marketDataAggregator).ingest(pt2);
        verify(marketDataAggregator).getAllBest();
        verifyNoMoreInteractions(marketDataAggregator);
    }

}
