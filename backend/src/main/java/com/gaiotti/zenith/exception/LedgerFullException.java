package com.gaiotti.zenith.exception;

public class LedgerFullException extends RuntimeException {
    public LedgerFullException(String message) {
        super(message);
    }
}
