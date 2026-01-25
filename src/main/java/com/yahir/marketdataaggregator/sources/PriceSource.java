package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;

import java.util.Optional;

public interface PriceSource {
    Optional<PriceTick> getLatestTick(String symbol);
    String name();
}
