package dev.gunn96.popcat.exception;

import dev.gunn96.popcat.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeoIpException.DatabaseInitializationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleDatabaseInitializationException(
            GeoIpException.DatabaseInitializationException e) {
        log.error("GeoIP database initialization failed. Message: {}", e.getMessage(), e);
        return ApiResponse.error("GEOIP_DATABASE_INIT_ERROR", e.getMessage());
    }

    @ExceptionHandler(GeoIpException.InvalidIpAddressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalidIpAddressException(
            GeoIpException.InvalidIpAddressException e) {
        log.error("Invalid IP address provided. Message: {}", e.getMessage(), e);
        return ApiResponse.error("INVALID_IP_ADDRESS", e.getMessage());
    }

    @ExceptionHandler(GeoIpException.DatabaseLookupException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleDatabaseLookupException(
            GeoIpException.DatabaseLookupException e) {
        log.error("GeoIP database lookup failed. Message: {}", e.getMessage(), e);
        return ApiResponse.error("GEOIP_LOOKUP_ERROR", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed. Errors: {}", errors);
        return ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAllUncaughtException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }
}