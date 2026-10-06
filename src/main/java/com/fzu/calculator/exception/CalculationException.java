package com.fzu.calculator.exception;

/**
 * Thrown when a user supplied expression cannot be evaluated.
 */
public class CalculationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public CalculationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
