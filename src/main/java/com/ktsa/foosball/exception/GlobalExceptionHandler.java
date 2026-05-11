package com.ktsa.foosball.exception;


import com.ktsa.foosball.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // JWT - Invalid/Malformed
        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error(401, "Invalid or malformed token", e.getMessage())
            );
        }

        // JWT - Expired (must be above JwtException since it's a subclass)
        @ExceptionHandler(ExpiredJwtException.class)
        public ResponseEntity<ApiResponse<?>> handleExpiredJwt(ExpiredJwtException e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error(401, "Token expired. Please login again.", null)
            );
        }

        // Illegal Argument
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error(401, "Invalid token or credentials", e.getMessage())
            );
        }

        // Resource Not Found
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error(404, e.getMessage(), null)
            );
        }

        // Validation
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
            List<String> validationErrors = e.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();

            return ResponseEntity.status(400).body(
                    ApiResponse.error(400, "Validation failed", validationErrors)
            );
        }

        // Generic Fallback - always keep this last
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error(500, "An unexpected error occurred", e.getMessage())
            );
        }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return ResponseEntity.status(401).body(
                ApiResponse.error(401, e.getMessage(), null)
        );
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountInactiveException(AccountInactiveException e) {
        return ResponseEntity.status(403).body(
                ApiResponse.error(403, e.getMessage(), null)
        );
    }
}

