package com.smartparking.exception;

/**
 * Exception thrown when an invalid slot state transition is attempted (e.g., reserving an occupied slot).
 */
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}

