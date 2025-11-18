package ru.yakovlev.walletapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import ru.yakovlev.walletapp.entity.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDTORequest {

    @NotNull(message = "Wallet ID is required")
    private UUID id;

    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
