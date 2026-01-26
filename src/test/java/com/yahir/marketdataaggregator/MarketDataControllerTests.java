package com.yahir.marketdataaggregator;

import com.yahir.marketdataaggregator.controller.MarketDataController;
import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MarketDataController.class)
public class MarketDataControllerTests {
    private final Instant fixedInstant = Instant.parse("2026-01-01T00:00:00.00Z");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketDataService marketDataService;

    @Test
    public void getBestPriceOfIsOkWhenBestExists() throws Exception {
        AggregatedPrice testBTC = new AggregatedPrice("BTCUSD", 35000, fixedClock.instant(), "TestSource");
        when(marketDataService.getBest("BTCUSD")).thenReturn(Optional.of(testBTC));

        mockMvc.perform(get("/prices/BTCUSD"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol").value("BTCUSD"))
                .andExpect(jsonPath("$.price").value(35000.0))
                .andExpect(jsonPath("$.timeStamp").value(fixedClock.instant().toString()))
                .andExpect(jsonPath("$.source").value("TestSource"));
    }

    @Test
    public void getBestPriceReturns404ErrorWhenBestDoesNotExist() throws Exception {
        when(marketDataService.getBest("BTCUSD")).thenReturn(Optional.empty());

        mockMvc.perform(get("/prices/BTCUSD"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Symbol not found with name: BTCUSD"));
    }

    @Test
    public void getBestPriceOfReturns400ErrorWhenSymbolIsNullOrEmpty() throws Exception {
        when(marketDataService.getBest(null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/prices/null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Symbol cannot be null or empty"));

        when(marketDataService.getBest("")).thenReturn(Optional.empty());

        mockMvc.perform(get("/prices/ "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Symbol cannot be null or empty"));
    }

    @Test
    public void getsAllBestPrices() throws Exception {
        AggregatedPrice testBTC = new AggregatedPrice("BTCUSD", 35000, fixedClock.instant(), "TestSource");
        AggregatedPrice testETH = new AggregatedPrice("ETHUSD", 5000, fixedClock.instant(), "TestSource");
        Map<String, AggregatedPrice> map = new HashMap<>();
        map.put("BTCUSD", testBTC);
        map.put("ETHUSD", testETH);

        when(marketDataService.getAllBest()).thenReturn(map);

        mockMvc.perform(get("/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].symbol", hasItems("BTCUSD", "ETHUSD")))
                .andExpect(jsonPath("$[?(@.symbol=='BTCUSD')].price", hasItem(35000.0)))
                .andExpect(jsonPath("$[?(@.symbol=='ETHUSD')].price", hasItem(5000.0)))
                .andExpect(jsonPath("$[?(@.symbol=='BTCUSD')].source", hasItem("TestSource")))
                .andExpect(jsonPath("$[?(@.symbol=='ETHUSD')].source", hasItem("TestSource")))
                .andExpect(jsonPath("$[?(@.symbol=='BTCUSD')].timeStamp", hasItem(fixedClock.instant().toString())))
                .andExpect(jsonPath("$[?(@.symbol=='ETHUSD')].timeStamp", hasItem(fixedClock.instant().toString())));
    }

    @Test
    public void getsAllPricesReturnsEmptyJsonListIfEmpty() throws Exception {
        Map<String, AggregatedPrice> map = new HashMap<>();
        when(marketDataService.getAllBest()).thenReturn(map);

        mockMvc.perform(get("/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getBestPrice_formatsCorrectly() throws Exception {
        AggregatedPrice testBTC = new AggregatedPrice("BTCUSD", 35000.656151, fixedClock.instant(), "TestSource");
        when(marketDataService.getBest("BTCUSD")).thenReturn(Optional.of(testBTC));

        mockMvc.perform(get("/prices/BTCUSD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(35000.66));
    }

    @Test
    public void pollOnceReturns400ErrorWhenSymbolIsNullOrEmpty() throws Exception {

        mockMvc.perform(post("/poll/ "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Symbol cannot be null or empty"));

        mockMvc.perform(post("/poll/null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Symbol cannot be null or empty"));

        verify(marketDataService, never()).pollOnce(anyString());
        verify(marketDataService, never()).getBest(anyString());
    }

    @Test
    public void pollOnceReturns404ErrorWhenSymbolIsNotFound() throws Exception {
        when(marketDataService.getBest("BTCUSD")).thenReturn(Optional.empty());

        mockMvc.perform(post("/poll/BTCUSD"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Symbol not found with name: BTCUSD"));

        verify(marketDataService, times(1)).pollOnce("BTCUSD");
        verify(marketDataService, times(1)).getBest("BTCUSD");
    }

    @Test
    public void pollOnceIsOkWhenSymbolExists() throws Exception {
        AggregatedPrice testBTC = new AggregatedPrice("BTCUSD", 35000, fixedClock.instant(), "TestSource");
        when(marketDataService.getBest("BTCUSD")).thenReturn(Optional.of(testBTC));

        mockMvc.perform(post("/poll/BTCUSD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTCUSD"))
                .andExpect(jsonPath("$.price").value(35000.0))
                .andExpect(jsonPath("$.timeStamp").value(fixedClock.instant().toString()))
                .andExpect(jsonPath("$.source").value("TestSource"));

        verify(marketDataService, times(1)).pollOnce("BTCUSD");
        verify(marketDataService, times(1)).getBest("BTCUSD");
    }


}
