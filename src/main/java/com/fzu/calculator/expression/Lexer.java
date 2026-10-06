package com.fzu.calculator.expression;

import com.fzu.calculator.exception.CalculationException;
import com.fzu.calculator.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a normalized expression string into a list of {@link Token}s.
 *
 * <p>The lexer is strict: any character outside the supported grammar (numbers, {@code + - * /},
 * parentheses and whitespace) raises an {@link ErrorCode#INVALID_EXPRESSION} error instead of being
 * ignored. This whitelist is the reason the evaluator never needs {@code eval}/{@code exec}.</p>
 */
public final class Lexer {

    private final String source;
    private int position;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<Token>();
        while (position < source.length()) {
            char current = source.charAt(position);
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }
            if (Character.isDigit(current) || current == '.') {
                tokens.add(readNumber());
                continue;
            }
            tokens.add(readSymbol(current));
        }
        tokens.add(new Token(TokenType.END_OF_INPUT, "", position + 1));
        return tokens;
    }

    private Token readNumber() {
        int start = position;
        boolean dotSeen = false;
        while (position < source.length()) {
            char current = source.charAt(position);
            if (Character.isDigit(current)) {
                position++;
            } else if (current == '.' && !dotSeen) {
                dotSeen = true;
                position++;
            } else {
                break;
            }
        }
        String text = source.substring(start, position);
        if (".".equals(text)) {
            throw new CalculationException(ErrorCode.INVALID_EXPRESSION,
                    "位置 " + (start + 1) + " 处的 '.' 不是有效数字");
        }
        return new Token(TokenType.NUMBER, text, start + 1);
    }

    private Token readSymbol(char current) {
        int start = position;
        position++;
        switch (current) {
            case '+':
                return new Token(TokenType.PLUS, "+", start + 1);
            case '-':
                return new Token(TokenType.MINUS, "-", start + 1);
            case '*':
                return new Token(TokenType.STAR, "*", start + 1);
            case '/':
                return new Token(TokenType.SLASH, "/", start + 1);
            case '(':
                return new Token(TokenType.LEFT_PAREN, "(", start + 1);
            case ')':
                return new Token(TokenType.RIGHT_PAREN, ")", start + 1);
            default:
                throw new CalculationException(ErrorCode.INVALID_EXPRESSION,
                        "位置 " + (start + 1) + " 处存在不支持的字符 '" + current + "'");
        }
    }
}
