package com.fzu.calculator.exception;

/**
 * Business error codes returned inside the standard API error body.
 */
public enum ErrorCode {

    /** The expression is empty, malformed or contains unsupported characters. */
    INVALID_EXPRESSION,

    /** The divisor evaluated to zero. */
    DIVISION_BY_ZERO,

    /** The request did not contain any expression. */
    EMPTY_EXPRESSION,

    /** The requested history record does not exist. */
    NOT_FOUND,

    /** Unexpected server side failure. */
    INTERNAL_ERROR
}
