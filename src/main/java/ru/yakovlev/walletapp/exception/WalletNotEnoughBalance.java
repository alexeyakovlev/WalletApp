package ru.yakovlev.walletapp.exception;

public class WalletNotEnoughBalance extends RuntimeException {
    public WalletNotEnoughBalance(String message) {
        super(message);
    }
}
