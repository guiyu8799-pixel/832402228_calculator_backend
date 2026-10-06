package com.fzu.calculator.exception;

/**
 * Thrown when a requested resource, for example a history record, does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
