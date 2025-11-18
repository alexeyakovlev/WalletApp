package ru.yakovlev.walletapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yakovlev.walletapp.dto.WalletDTORequest;
import ru.yakovlev.walletapp.dto.WalletDTOResponse;
import ru.yakovlev.walletapp.entity.OperationType;
import ru.yakovlev.walletapp.entity.Wallet;
import ru.yakovlev.walletapp.exception.WalletNotEnoughBalance;
import ru.yakovlev.walletapp.exception.WalletNotFoundException;
import ru.yakovlev.walletapp.repository.WalletRepository;
import ru.yakovlev.walletapp.util.WalletMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public List<WalletDTOResponse> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(WalletMapper::toWalletDTOResponse)
                .collect(Collectors.toList());
    }

    public WalletDTOResponse getWalletById(UUID id) throws WalletNotFoundException {
        Wallet walletById = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        return WalletMapper.toWalletDTOResponse(walletById);
    }

    @Transactional
    public WalletDTOResponse createNewWallet() {
        Wallet walletToSave = new Wallet();
        walletRepository.save(walletToSave);
        return WalletMapper.toWalletDTOResponse(walletToSave);
    }

    @Transactional
    public void deleteWalletById(UUID id) throws WalletNotFoundException {
        Wallet walletToId = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        walletRepository.delete(walletToId);
    }

    @Transactional
    public WalletDTOResponse depositOrWithdraw(WalletDTORequest walletDTORequest)
            throws WalletNotFoundException, WalletNotEnoughBalance {
        Wallet exsistWallet = walletRepository.findById(walletDTORequest.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + walletDTORequest.getId() + " not found"));

        if (walletDTORequest.getOperationType() == OperationType.DEPOSIT) {
            exsistWallet.setBalance(exsistWallet.getBalance().add(walletDTORequest.getAmount()));
        } else if (walletDTORequest.getOperationType() == OperationType.WITHDRAW) {
            if (exsistWallet.getBalance().compareTo(walletDTORequest.getAmount()) < 0) {
                throw new WalletNotEnoughBalance("Wallet with id " + walletDTORequest.getId() + " not enough balance");
            }
            exsistWallet.setBalance(exsistWallet.getBalance().subtract(walletDTORequest.getAmount()));
        }
        walletRepository.save(exsistWallet);
        return WalletMapper.toWalletDTOResponse(exsistWallet);
    }
}
