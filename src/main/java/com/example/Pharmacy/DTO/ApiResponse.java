package com.example.Pharmacy.DTO;

import lombok.Builder;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.util.List;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private List<FieldError> errors;

    public static <T> ApiResponse<T> ok (T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false).message(message).errorCode(errorCode).build();
    }
}
