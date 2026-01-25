package com.yahir.marketdataaggregator.service;

import com.yahir.marketdataaggregator.MarketDataAggregator;
import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import com.yahir.marketdataaggregator.sources.PriceSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketDataService {
    private final List<PriceSource> priceSources;
    private final MarketDataAggregator aggregator;

    public MarketDataService(List<PriceSource> priceSources, MarketDataAggregator aggregator) {
        this.priceSources = priceSources;
        this.aggregator = aggregator;
    }

    public void pollOnce(String symbol) {
        if (priceSources.isEmpty()) {
            return;
        }
        for (PriceSource source : priceSources) {
            Optional<PriceTick> tick = source.getLatestTick(symbol);
            if (tick.isPresent()) {
                aggregator.ingest(tick.get());
            }
        }

    }

    public Optional<AggregatedPrice> getBest(String symbol) {
        return aggregator.getBest(symbol);
    }

    public Map<String,AggregatedPrice> getAllBest () {
        return aggregator.getAllBest();
    }


}
