package ru.yakovlev.walletapp.exception;

public record ErrorResponse(String error, String message) {
    @Override
    public String toString() {
        return "Error: " + error + "\n" + "Message: " + message;
    }
}