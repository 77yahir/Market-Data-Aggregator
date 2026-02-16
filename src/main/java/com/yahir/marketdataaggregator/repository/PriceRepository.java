package com.yahir.marketdataaggregator.repository;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<AggregatedPrice, Long> {

}
