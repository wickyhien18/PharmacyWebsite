package Pharmacy.Exceptions;

import Pharmacy.DTO.Response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
/**
 * Class GlobalExceptionHandler.
 * Provides functionality and data modeling for GlobalExceptionHandler.
 */
public class GlobalExceptionHandler {

    // ---- Validation lỗi (@Valid) → 422 Unprocessable Entity ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid",
                        (a, b) -> a  // Giữ lỗi đầu tiên nếu 1 field có nhiều lỗi
                ));

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, "Invalid Data", errors));
    }

    // ---- Auth lỗi → 401 ----
    @ExceptionHandler(AuthException.class)
    /**
     * Handle auth.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    // ---- Không có quyền → 403 ----
    @ExceptionHandler(AccessDeniedException.class)
    /**
     * Handle access denied.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("You don't have permission"));
    }

    // ---- Resource không tìm thấy → 404 ----
    @ExceptionHandler(ResourceNotFoundException.class)
    /**
     * Handle not found.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    // ---- Business logic lỗi → 400 ----
    @ExceptionHandler(BusinessException.class)
    /**
     * Handle business.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    /**
     * Handle conflict.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleConflict(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    // ---- Catch-all → 500 (không lộ stack trace ra client) ----
    @ExceptionHandler(Exception.class)
    /**
     * Handle general.
     *
     * @param ex the ex
     * @return the ResponseEntity<ApiResponse<Void>> result
     */
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Error from database. Please retry"));
    }
}