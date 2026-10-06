package com.fzu.calculator.expression;

/**
 * A single lexical token with its raw text and its one based position in the source expression.
 */
public final class Token {

    private final TokenType type;
    private final String text;
    private final int position;

    public Token(TokenType type, String text, int position) {
        this.type = type;
        this.text = text;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "('" + text + "')@" + position;
    }
}
