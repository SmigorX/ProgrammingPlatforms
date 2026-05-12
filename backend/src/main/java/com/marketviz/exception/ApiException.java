package com.marketviz.exception;

import org.springframework.http.HttpStatus;

/**
 * Base runtime exception carrying an HTTP status code.
 *
 * <p>Subclasses map to specific HTTP semantics (404, 409, …).
 * {@link GlobalExceptionHandler} translates any {@code ApiException} into an
 * RFC 7807 {@link org.springframework.http.ProblemDetail} response.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
