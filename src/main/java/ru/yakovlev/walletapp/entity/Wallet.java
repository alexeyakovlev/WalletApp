package ru.yakovlev.walletapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name = "wallet_id")
    private UUID id;

    @Column(name = "balance")
    private BigDecimal balance =  BigDecimal.ZERO;

}
