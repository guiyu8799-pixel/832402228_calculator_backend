package com.fzu.calculator.expression;

import com.fzu.calculator.exception.CalculationException;
import com.fzu.calculator.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * A hand written recursive descent evaluator for arithmetic expressions.
 *
 * <p>Supported grammar (from lowest to highest precedence):</p>
 * <pre>
 *   expression := term (('+' | '-') term)*
 *   term       := unary (('*' | '/') unary)*
 *   unary      := ('+' | '-') unary | primary
 *   primary    := NUMBER
 *               | '(' expression ')'
 * </pre>
 *
 * <p>Operator precedence is expressed by the grammar itself, so no separate precedence table is
 * needed. No user input is ever passed to {@code eval}, {@code exec}, {@code ScriptEngine} or any
 * other code execution facility: the lexer only accepts numbers, the four operators and
 * parentheses.</p>
 */
public final class ExpressionCalculator {

    /** Precision used for every intermediate operation (34 significant digits). */
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private ExpressionCalculator() {
    }

    /**
     * Evaluates an already normalized expression.
     *
     * @param expression expression such as {@code "(1+2)*3"}
     * @return the result, trailing zeros removed
     * @throws CalculationException when the expression is malformed or not computable
     */
    public static BigDecimal evaluate(String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new CalculationException(ErrorCode.EMPTY_EXPRESSION, "表达式不能为空");
        }
        List<Token> tokens = new Lexer(expression).tokenize();
        Parser parser = new Parser(tokens, expression);
        BigDecimal value = parser.parseExpression();
        parser.expectEnd();
        return normalize(value);
    }

    /** Removes meaningless trailing zeros, for example {@code 5.00} becomes {@code 5}. */
    private static BigDecimal normalize(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 0) {
            stripped = stripped.setScale(0, RoundingMode.UNNECESSARY);
        }
        return stripped;
    }

    private static BigDecimal divide(BigDecimal left, BigDecimal right) {
        if (right.signum() == 0) {
            throw new CalculationException(ErrorCode.DIVISION_BY_ZERO, "除数不能为 0");
        }
        return left.divide(right, MATH_CONTEXT);
    }

    /** Recursive descent parser operating on the token stream. */
    private static final class Parser {

        private final List<Token> tokens;
        private final String expression;
        private int index;

        Parser(List<Token> tokens, String expression) {
            this.tokens = tokens;
            this.expression = expression;
        }

        /** Lowest precedence: addition and subtraction. */
        BigDecimal parseExpression() {
            BigDecimal left = parseTerm();
            while (true) {
                TokenType type = peek().getType();
                if (type == TokenType.PLUS) {
                    advance();
                    left = left.add(parseTerm(), MATH_CONTEXT);
                } else if (type == TokenType.MINUS) {
                    advance();
                    left = left.subtract(parseTerm(), MATH_CONTEXT);
                } else {
                    return left;
                }
            }
        }

        /** Higher precedence: multiplication and division. */
        private BigDecimal parseTerm() {
            BigDecimal left = parseUnary();
            while (true) {
                TokenType type = peek().getType();
                if (type == TokenType.STAR) {
                    advance();
                    left = left.multiply(parseUnary(), MATH_CONTEXT);
                } else if (type == TokenType.SLASH) {
                    advance();
                    left = divide(left, parseUnary());
                } else {
                    return left;
                }
            }
        }

        /**
         * Unary plus and minus.
         *
         * <p>Because this level sits between the term level and the primary level, a sign that
         * appears where an operand is expected (for example {@code 3*-2}) is treated as a sign
         * rather than as a binary operator.</p>
         */
        private BigDecimal parseUnary() {
            TokenType type = peek().getType();
            if (type == TokenType.PLUS) {
                advance();
                return parseUnary();
            }
            if (type == TokenType.MINUS) {
                advance();
                return parseUnary().negate(MATH_CONTEXT);
            }
            return parsePrimary();
        }

        private BigDecimal parsePrimary() {
            Token token = peek();
            switch (token.getType()) {
                case NUMBER:
                    advance();
                    try {
                        return new BigDecimal(token.getText(), MATH_CONTEXT);
                    } catch (NumberFormatException ex) {
                        throw new CalculationException(ErrorCode.INVALID_EXPRESSION,
                                "无效的数字 '" + token.getText() + "'");
                    }
                case LEFT_PAREN:
                    advance();
                    BigDecimal inner = parseExpression();
                    expect(TokenType.RIGHT_PAREN, "缺少右括号 ')'");
                    return inner;
                default:
                    throw new CalculationException(ErrorCode.INVALID_EXPRESSION,
                            "表达式 '" + expression + "' 中存在无法识别的内容 '"
                                    + token.getText() + "'");
            }
        }

        private void expect(TokenType expected, String message) {
            if (peek().getType() != expected) {
                throw new CalculationException(ErrorCode.INVALID_EXPRESSION, message);
            }
            advance();
        }

        /** Rejects trailing content, for example the {@code )} in {@code "1+2)"}. */
        void expectEnd() {
            if (peek().getType() != TokenType.END_OF_INPUT) {
                throw new CalculationException(ErrorCode.INVALID_EXPRESSION,
                        "表达式 '" + expression + "' 中存在无法识别的内容 '"
                                + peek().getText() + "'");
            }
        }

        private Token peek() {
            return tokens.get(index);
        }

        private void advance() {
            if (index < tokens.size() - 1) {
                index++;
            }
        }
    }
}
