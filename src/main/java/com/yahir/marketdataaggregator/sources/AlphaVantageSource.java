package com.yahir.marketdataaggregator.sources;

import com.yahir.marketdataaggregator.domain.PriceTick;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(
        prefix = "sources.alphavantage",
        name = "enabled",
        havingValue = "true"
)
public class AlphaVantageSource implements PriceSource {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public AlphaVantageSource(RestTemplate restTemplate, @Value("${alphavantage.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public Optional<PriceTick> getLatestTick(String symbol) {
        try {
            String url = String.format(
                    "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    symbol,
                    apiKey
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            Map<String, String> quote = (Map<String, String>) response.get("Global Quote");

            String priceStr = quote.get("05. price");
            double price = Double.parseDouble(priceStr);

            PriceTick tick = new PriceTick(
                    symbol,
                    price,
                    Instant.now(),
                    "AlphaVantage"
            );

            return Optional.of(tick);
        } catch (Exception e) {
            // If anything goes wrong (API down, bad response, etc..) return empty
            System.err.println("Error fetching from Alpha Vantage: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String name() {
        return "AlphaVantage";
    }
}
