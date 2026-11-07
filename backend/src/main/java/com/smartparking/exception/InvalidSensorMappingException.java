package com.smartparking.exception;

/**
 * Exception thrown when a sensor ID does not match the configured mapping for a parking slot.
 */
public class InvalidSensorMappingException extends RuntimeException {
    public InvalidSensorMappingException(String message) {
        super(message);
    }
}

