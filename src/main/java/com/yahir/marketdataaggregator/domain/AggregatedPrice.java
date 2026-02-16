package com.yahir.marketdataaggregator.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.Objects;

@Entity
public class AggregatedPrice {
    private String symbol;
    private double price;
    private Instant timeStamp;
    private String source;
    private String reason;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public AggregatedPrice(String symbol, double price, Instant timeStamp, String source, String reason) {
        this.symbol = symbol.trim().toUpperCase();
        this.price = price;
        this.timeStamp = timeStamp;
        this.source = source;
        this.reason = reason;
    }

    public AggregatedPrice(String symbol, double price, Instant timeStamp, String source) {
        this.symbol = symbol.trim().toUpperCase();
        this.price = price;
        this.timeStamp = timeStamp;
        this.source = source;
    }

    public AggregatedPrice(PriceTick priceTick) {
        this.symbol = priceTick.getSymbol();
        this.price = priceTick.getPrice();
        this.timeStamp = priceTick.getTimeStamp();
        this.source = priceTick.getSource();
    }

    public AggregatedPrice() {
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        AggregatedPrice other = (AggregatedPrice) o;
        return Double.compare(other.price, price) == 0
                && Objects.equals(symbol, other.symbol)
                && Objects.equals(timeStamp, other.timeStamp)
                && Objects.equals(source, other.source)
                && Objects.equals(reason, other.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, price, timeStamp, source, reason);
    }
}
