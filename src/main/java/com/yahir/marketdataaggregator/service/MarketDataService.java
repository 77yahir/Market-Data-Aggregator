package com.yahir.marketdataaggregator.service;

import com.yahir.marketdataaggregator.MarketDataAggregator;
import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.domain.PriceTick;
import com.yahir.marketdataaggregator.sources.PriceSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketDataService {
    private final List<PriceSource> priceSources;
    private final MarketDataAggregator aggregator;
    private final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    public MarketDataService(List<PriceSource> priceSources, MarketDataAggregator aggregator) {
        this.priceSources = priceSources;
        this.aggregator = aggregator;
    }

    public void pollOnce(String symbol) {
        if (priceSources.isEmpty()) {
            log.warn("pollOnce called but no sources found for symbol={}", symbol);
            return;
        }
        log.info("pollOnce start for symbol={}, sources={}", symbol, priceSources.size());
        for (PriceSource source : priceSources) {
            Optional<PriceTick> tick = source.getLatestTick(symbol);
            log.debug("source={} returned tickPresent={}", source.name(), tick.isPresent());

            if (tick.isPresent()) {
                PriceTick priceTick = tick.get();
                log.info("forwarding tick to aggregator: symbol={}, price={}, ts={}, source={}",
                        symbol, priceTick.getPrice() ,priceTick.getTimeStamp(), priceTick.getSource());
                aggregator.ingest(priceTick);
            }
        }
        log.info("pollOnce complete for symbol={},", symbol);
    }

    public Optional<AggregatedPrice> getBest(String symbol) {
        return aggregator.getBest(symbol);
    }

    public Map<String,AggregatedPrice> getAllBest () {
        return aggregator.getAllBest();
    }


}
