package dev.gunn96.popcat.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    public static <T> ApiResponse<T> error(String code, String message, T details) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message, details));
    }
}

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse<T> {
    private final String code;
    private final String message;
    private T details;

    public ErrorResponse(String code, String message, T details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}