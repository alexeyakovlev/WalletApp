package ru.yakovlev.walletapp.exception;

public class WalletNotEnoughBalance extends Exception {
    public WalletNotEnoughBalance(String message) {
        super(message);
    }
}
