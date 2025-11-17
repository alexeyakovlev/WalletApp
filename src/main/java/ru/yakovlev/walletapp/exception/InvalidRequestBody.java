package ru.yakovlev.walletapp.exception;

public class InvalidRequestBody extends RuntimeException {
    public InvalidRequestBody(String message) {
        super(message);
    }
}
