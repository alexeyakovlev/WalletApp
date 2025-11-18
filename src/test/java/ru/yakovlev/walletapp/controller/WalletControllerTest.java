package ru.yakovlev.walletapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yakovlev.walletapp.dto.WalletDTORequest;
import ru.yakovlev.walletapp.dto.WalletDTOResponse;
import ru.yakovlev.walletapp.entity.OperationType;
import ru.yakovlev.walletapp.exception.GlobalExceptionHandler;
import ru.yakovlev.walletapp.exception.WalletNotEnoughBalance;
import ru.yakovlev.walletapp.exception.WalletNotFoundException;
import ru.yakovlev.walletapp.service.WalletService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private ObjectMapper objectMapper;
    private UUID walletId;
    private WalletDTOResponse walletResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        walletId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        walletResponse = new WalletDTOResponse();
        walletResponse.setId(walletId);
        walletResponse.setBalance(BigDecimal.valueOf(1000.00)); // Явно указываем дробную часть
    }

    @Test
    void getAllWallets_ShouldReturnListOfWallets() throws Exception {
        WalletDTOResponse wallet2 = new WalletDTOResponse();
        wallet2.setId(UUID.randomUUID());
        wallet2.setBalance(BigDecimal.valueOf(500.50)); // Добавляем дробную часть

        List<WalletDTOResponse> wallets = Arrays.asList(walletResponse, wallet2);
        when(walletService.getAllWallets()).thenReturn(wallets);

        mockMvc.perform(get("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(walletResponse.getId().toString())))
                .andExpect(jsonPath("$[0].balance", is(1000.00))) // Теперь будет работать
                .andExpect(jsonPath("$[1].balance", is(500.50)));

        verify(walletService, times(1)).getAllWallets();
    }

    @Test
    void getWalletById_WithExistingId_ShouldReturnWallet() throws Exception {
        when(walletService.getWalletById(walletId)).thenReturn(walletResponse);

        mockMvc.perform(get("/api/v1/wallets/{WALLET_UUID}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(walletId.toString())))
                .andExpect(jsonPath("$.balance", is(1000.00))); // Исправлено

        verify(walletService, times(1)).getWalletById(walletId);
    }

    @Test
    void getWalletById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(walletService.getWalletById(walletId))
                .thenThrow(new WalletNotFoundException("Wallet with id " + walletId + " not found"));

        mockMvc.perform(get("/api/v1/wallets/{WALLET_UUID}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Wallet with id " + walletId + " not found")));

        verify(walletService, times(1)).getWalletById(walletId);
    }

    @Test
    void createWallet_ShouldReturnCreatedWallet() throws Exception {
        WalletDTOResponse newWallet = new WalletDTOResponse();
        newWallet.setId(UUID.randomUUID());
        newWallet.setBalance(BigDecimal.valueOf(0.00)); // Явно указываем дробную часть

        when(walletService.createNewWallet()).thenReturn(newWallet);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.balance", is(0.00))); // Исправлено

        verify(walletService, times(1)).createNewWallet();
    }

    @Test
    void deleteWalletById_WithExistingId_ShouldDeleteWallet() throws Exception {
        doNothing().when(walletService).deleteWalletById(walletId);

        mockMvc.perform(delete("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletId)))
                .andExpect(status().isOk());

        verify(walletService, times(1)).deleteWalletById(walletId);
    }

    @Test
    void deleteWalletById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        doThrow(new WalletNotFoundException("Wallet not found"))
                .when(walletService).deleteWalletById(walletId);

        mockMvc.perform(delete("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Wallet not found")));

        verify(walletService, times(1)).deleteWalletById(walletId);
    }

    @Test
    void depositOrWithdrawal_DepositOperation_ShouldIncreaseBalance() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(500.00));
        WalletDTOResponse updatedWallet = new WalletDTOResponse(walletId, BigDecimal.valueOf(1500.00));

        when(walletService.depositOrWithdraw(any(WalletDTORequest.class))).thenReturn(updatedWallet);

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(walletId.toString())))
                .andExpect(jsonPath("$.balance", is(1500.00))); // Исправлено

        verify(walletService, times(1)).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithdrawOperation_ShouldDecreaseBalance() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.WITHDRAW, BigDecimal.valueOf(300.00));
        WalletDTOResponse updatedWallet = new WalletDTOResponse(walletId, BigDecimal.valueOf(700.00));

        when(walletService.depositOrWithdraw(any(WalletDTORequest.class))).thenReturn(updatedWallet);

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(walletId.toString())))
                .andExpect(jsonPath("$.balance", is(700.00))); // Исправлено

        verify(walletService, times(1)).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithNonExistingWallet_ShouldReturnNotFound() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(100.00));

        when(walletService.depositOrWithdraw(any(WalletDTORequest.class)))
                .thenThrow(new WalletNotFoundException("Wallet with id " + walletId + " not found"));

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Wallet with id " + walletId + " not found")));

        verify(walletService, times(1)).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithInsufficientBalance_ShouldReturnConflict() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.WITHDRAW, BigDecimal.valueOf(2000.00));

        when(walletService.depositOrWithdraw(any(WalletDTORequest.class)))
                .thenThrow(new WalletNotEnoughBalance("Wallet with id " + walletId + " not enough balance"));

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("not enough balance")));

        verify(walletService, times(1)).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(walletService, never()).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "id": "%s"
            }
            """.formatted(walletId);

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(walletService, never()).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void getAllWallets_WhenNoWallets_ShouldReturnEmptyList() throws Exception {
        when(walletService.getAllWallets()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(walletService, times(1)).getAllWallets();
    }

    @Test
    void depositOrWithdrawal_WithPositiveAmount_ShouldWorkCorrectly() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(0.01));
        when(walletService.depositOrWithdraw(any(WalletDTORequest.class))).thenReturn(walletResponse);

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(walletId.toString())));

        verify(walletService, times(1)).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithNullAmount_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "id": "%s",
                "operationType": "DEPOSIT",
                "amount": null
            }
            """.formatted(walletId);

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", is("Amount is required")));

        verify(walletService, never()).depositOrWithdraw(any(WalletDTORequest.class));
    }

    @Test
    void depositOrWithdrawal_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        WalletDTORequest request = new WalletDTORequest(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(-100.00));

        mockMvc.perform(put("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", is("Amount must be positive")));

        verify(walletService, never()).depositOrWithdraw(any(WalletDTORequest.class));
    }
}