package com.yahir.marketdataaggregator.exception;

public class SymbolNotFoundException extends RuntimeException {

    public SymbolNotFoundException(String message) {
        super(message);
    }

}
