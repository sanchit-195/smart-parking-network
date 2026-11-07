package com.smartparking.exception;

/**
 * Exception thrown when a requested resource (e.g., parking slot) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

