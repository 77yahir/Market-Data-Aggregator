package com.yahir.marketdataaggregator.controller;

import com.yahir.marketdataaggregator.domain.AggregatedPrice;
import com.yahir.marketdataaggregator.repository.PriceRepository;
import com.yahir.marketdataaggregator.service.MarketDataService;
import org.apache.coyote.Response;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
public class MarketDataController {

    private final PriceRepository priceRepository;

    public record ResponseDTO(String symbol, BigDecimal price, Instant timeStamp, String source){}
    public record ApiError(String message) {}

    private final MarketDataService marketDataService;

    @Autowired
    public MarketDataController(MarketDataService service, PriceRepository priceRepository) {
        this.marketDataService = service;
        this.priceRepository = priceRepository;
    }

    @GetMapping("/prices/{symbol}")
    public ResponseEntity<?> getBestPriceOf(@PathVariable String symbol) {

        if (isEmptyOrNull(symbol)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError("Symbol cannot be null or empty"));
        }
        return getResponseEntity(symbol);
    }

    @GetMapping("/prices")
    public List<ResponseDTO> getAllPrices() {
        List<ResponseDTO> ResponseDTOS = new ArrayList<>();
        Map<String, AggregatedPrice> tempMap = marketDataService.getAllBest();
        tempMap.forEach((symbol, price) -> ResponseDTOS.add(toDTO(price)));
        return ResponseDTOS;
    }

    @GetMapping("/symbols")
    public List<String> getAllSymbols() {
        List<String> symbolList = new ArrayList<>();
        Map<String, AggregatedPrice> tempMap = marketDataService.getAllBest();
        tempMap.forEach((symbol, price) -> symbolList.add(symbol));
        return symbolList;
    }

    @PostMapping("/poll/{symbol}")
    public ResponseEntity<?> pollOnce(@PathVariable String symbol) {

        if (isEmptyOrNull(symbol)) {
            return ResponseEntity.
                    status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError("Symbol cannot be null or empty"));
        }

        marketDataService.pollOnce(symbol);

        return getResponseEntity(symbol);
    }

    @GetMapping("/prices/history")
    public List<ResponseDTO> getAllPricesFromDataBase() {
        List<ResponseDTO> responseDTOS = new ArrayList<>();
        List<AggregatedPrice> tempList = marketDataService.getAllPrices();
        tempList.forEach((price) -> responseDTOS.add(toDTO(price)));
        return responseDTOS;
    }

    @GetMapping("/prices/history/{symbol}")
    public List<ResponseDTO> getSymbolHistory(@PathVariable String symbol) {
        List<ResponseDTO> responseDTOS = new ArrayList<>();
        List<AggregatedPrice> tempList = marketDataService.getAllPricesForSymbol(symbol);
        tempList.forEach((price) -> responseDTOS.add(toDTO(price)));
        return responseDTOS;
    }

    @GetMapping("/prices/history/range")
    public List<ResponseDTO> getPricesByTimeRange(
            @RequestParam String inStart,
            @RequestParam String inEnd
    ) {
        List<ResponseDTO> responseDTOS = new ArrayList<>();
        Instant start = Instant.parse(inStart);
        Instant end = Instant.parse(inEnd);
        List<AggregatedPrice> tempList = marketDataService.getAllPricesBetween(start, end);
        tempList.forEach((price) -> responseDTOS.add(toDTO(price)));
        return responseDTOS;
    }

    @GetMapping("/prices/history/{symbol}/range")
    public List<ResponseDTO> getSymbolHistoryInRange(
            @PathVariable String symbol,
            @RequestParam String inStart,
            @RequestParam String inEnd
    ) {
        List<ResponseDTO> responseDTOS = new ArrayList<>();
        Instant start = Instant.parse(inStart);
        Instant end = Instant.parse(inEnd);
        List<AggregatedPrice> tempList = marketDataService.getPriceHistoryForSymbolBetween(symbol, start, end);
        tempList.forEach((price) -> responseDTOS.add(toDTO(price)));
        return responseDTOS;
    }

    @NonNull
    private ResponseEntity<?> getResponseEntity(@PathVariable String symbol) {
        Optional<AggregatedPrice> best = marketDataService.getBest(symbol);

        if (best.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Symbol not found with name: " + symbol));
        }
        ResponseDTO dto = toDTO(best.get());
        return ResponseEntity.ok(dto);
    }

    private boolean isEmptyOrNull(String symbol) {
        return symbol == null || symbol.isBlank() || symbol.equalsIgnoreCase("null");
    }

    private ResponseDTO toDTO(AggregatedPrice price) {
        BigDecimal priceAsDecimal = new BigDecimal(price.getPrice()).setScale(2, RoundingMode.UP);
        return new ResponseDTO(price.getSymbol(), priceAsDecimal, price.getTimeStamp(), price.getSource());
    }
}
