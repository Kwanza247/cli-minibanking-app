package com.minibank.exception;

public class BankException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BankException(String message) {
        super(message);
    }
}