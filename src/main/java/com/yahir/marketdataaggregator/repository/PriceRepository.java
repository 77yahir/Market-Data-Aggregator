package com.yahir.marketdataaggregator.repository;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<AggregatedPrice, Long> {

    List<AggregatedPrice> findBySymbol(String symbol);
    List<AggregatedPrice> findByTimeStampBetween(Instant start, Instant end);
    List<AggregatedPrice> findBySymbolAndTimeStampBetween(String symbol, Instant start, Instant end);

}
