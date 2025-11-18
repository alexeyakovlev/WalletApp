package ru.yakovlev.walletapp.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yakovlev.walletapp.dto.WalletDTORequest;
import ru.yakovlev.walletapp.dto.WalletDTOResponse;
import ru.yakovlev.walletapp.exception.WalletNotEnoughBalance;
import ru.yakovlev.walletapp.exception.WalletNotFoundException;
import ru.yakovlev.walletapp.service.WalletService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/wallets")
    @ResponseStatus(HttpStatus.OK)
    public List<WalletDTOResponse> getAllWallets() {
        return walletService.getAllWallets();
    }

    @GetMapping("/wallets/{WALLET_UUID}")
    @ResponseStatus(HttpStatus.OK)
    public WalletDTOResponse getWalletById(@PathVariable UUID WALLET_UUID)  throws WalletNotFoundException {
        return walletService.getWalletById(WALLET_UUID);
    }

    @PostMapping("/wallet")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletDTOResponse createWallet() {
        return walletService.createNewWallet();
    }

    @DeleteMapping("/wallet")
    @ResponseStatus(HttpStatus.OK)
    public void deleteWalletById(@RequestBody UUID WALLET_UUID)  throws WalletNotFoundException {
        walletService.deleteWalletById(WALLET_UUID);
    }

    @PutMapping("/wallet")
    @ResponseStatus(HttpStatus.OK)
    public WalletDTOResponse depositOrWithdrawal(@RequestBody @Valid WalletDTORequest walletDTO)
            throws WalletNotFoundException, WalletNotEnoughBalance {
        return walletService.depositOrWithdraw(walletDTO);
    }
}
