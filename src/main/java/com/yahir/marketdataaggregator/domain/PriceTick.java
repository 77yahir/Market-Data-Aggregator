package com.yahir.marketdataaggregator.domain;

import java.time.Instant;

public class PriceTick {
    private String symbol;
    private double price;
    private Instant timeStamp;
    private String source;

    public PriceTick() {
        this.symbol = null;
        this.price = 0;
        this.timeStamp = null;
        this.source = null;
    }

    public PriceTick(String symbol, double price, Instant timeStamp, String source) {
        this.symbol = symbol;
        this.price = price;
        this.timeStamp = timeStamp;
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isEmpty() {
        return symbol == null && price == 0 && timeStamp == null && source == null;
    }
}
