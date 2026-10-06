package com.fzu.calculator.dto;

import com.fzu.calculator.exception.ErrorCode;

/**
 * Standard error body, for example:
 * <pre>
 * {
 *   "success": false,
 *   "errorCode": "DIVISION_BY_ZERO",
 *   "message": "Division by zero is not allowed"
 * }
 * </pre>
 */
public class ErrorResponse {

    private final boolean success = false;
    private ErrorCode errorCode;
    private String message;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
