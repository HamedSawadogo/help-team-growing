package com.commerceops.common;

import com.commerceops.inventory.InsufficientStockException;
import com.commerceops.payments.OverpaymentException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

// Deliberately not exhaustive: only the exception types below get a clean,
// consistent JSON body. Anything else falls through to Spring Boot's default
// error response - which is part of the "inconsistent error handling" lab.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
    }

    @ExceptionHandler(OverpaymentException.class)
    public ResponseEntity<Map<String, Object>> handleOverpayment(OverpaymentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body(ex.getMessage()));
    }

    private Map<String, Object> body(String message) {
        return Map.of("timestamp", LocalDateTime.now().toString(), "message", message);
    }
}
