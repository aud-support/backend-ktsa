package com.ktsa.foosball.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private Object errors;
    private LocalDateTime timestamp;

    private ApiResponse(boolean success, int status, String message, T data, Object errors) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    // SUCCESS RESPONSE
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, message, data, null);
    }

    // ERROR RESPONSE
    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return new ApiResponse<>(false, status, message, null, errors);
    }
}
