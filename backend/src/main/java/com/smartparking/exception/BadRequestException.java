package com.smartparking.exception;

/**
 * Exception thrown when a request has invalid or unsupported parameters.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

