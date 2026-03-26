package com.wallet.vexspend.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private Map<String, String> validationErrors;

    public Instant timestamp() {
        return timestamp;
    }

    public int status() {
        return status;
    }

    public String error() {
        return error;
    }

    public String message() {
        return message;
    }

    public String path() {
        return path;
    }

    public Map<String, String> validationErrors() {
        return validationErrors;
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError validation(int status, String error, String message, String path,
                                      Map<String, String> validationErrors) {
        return new ApiError(Instant.now(), status, error, message, path, validationErrors);
    }
}



