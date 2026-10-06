package com.fzu.calculator.expression;

/**
 * Token kinds produced by the {@link Lexer}.
 */
public enum TokenType {
    NUMBER,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    LEFT_PAREN,
    RIGHT_PAREN,
    END_OF_INPUT
}
