package ru.yakovlev.walletapp.util;

import ru.yakovlev.walletapp.dto.WalletDTOResponse;
import ru.yakovlev.walletapp.entity.Wallet;

public class WalletMapper {

    public static WalletDTOResponse toWalletDTOResponse(Wallet wallet) {
        WalletDTOResponse walletDTOResponse = new WalletDTOResponse();
        walletDTOResponse.setId(wallet.getId());
        walletDTOResponse.setBalance(wallet.getBalance());
        return walletDTOResponse;
    }
}
