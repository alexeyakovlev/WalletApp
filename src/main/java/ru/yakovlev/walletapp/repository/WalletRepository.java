package ru.yakovlev.walletapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yakovlev.walletapp.entity.Wallet;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
