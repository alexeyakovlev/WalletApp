package ru.yakovlev.walletapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleWalletNotFoundException(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(WalletNotEnoughBalance.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleWalletNotEnoughBalance(WalletNotEnoughBalance ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        Throwable cause = ex.getCause();

        if (message != null && (message.contains("UUID") ||
                message.contains("java.util.UUID") ||
                (cause != null && cause.getMessage() != null && cause.getMessage().contains("UUID")))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid UUID format");
        }

        if (message != null && message.contains("OperationType")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid operation type. Allowed values: DEPOSIT, WITHDRAW");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request body format");
    }
}
