package com.fzu.calculator.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fzu.calculator.exception.CalculationException;
import com.fzu.calculator.exception.ErrorCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests of the expression engine (lexer + recursive descent parser).
 *
 * <p>They cover exactly what the assignment requires: the four basic operations, operator
 * precedence, parentheses, unary plus and minus, decimal numbers, invalid expressions and
 * division by zero.</p>
 */
class ExpressionCalculatorTest {

    private static void assertEvaluates(String expression, String expected) {
        BigDecimal actual = ExpressionCalculator.evaluate(ExpressionNormalizer.normalize(expression));
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Expected " + expression + " = " + expected + " but got " + actual);
    }

    private static void assertFails(String expression, ErrorCode expectedCode) {
        CalculationException ex = assertThrows(CalculationException.class,
                () -> ExpressionCalculator.evaluate(ExpressionNormalizer.normalize(expression)),
                "Expected " + expression + " to be rejected");
        assertEquals(expectedCode, ex.getErrorCode(), "Wrong error code for " + expression);
    }

    @Nested
    @DisplayName("Function 1 - the four basic operations")
    class BasicOperations {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "12+8, 20",
                "9-4, 5",
                "6*7, 42",
                "20/4, 5",
                "0+0, 0",
                "100-250, -150",
                "7/2, 3.5"
        })
        void evaluatesBasicOperations(String expression, String expected) {
            assertEvaluates(expression, expected);
        }
    }

    @Nested
    @DisplayName("Function 2 - compound expressions")
    class CompoundExpressions {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                // Operator precedence.
                "1+2*3, 7",
                "8-3*2, 2",
                "2+3*4-5, 9",
                "100/10/2, 5",
                "2*3+4*5, 26",
                // Parentheses.
                "(1+2)*3, 9",
                "(2+3)*4, 20",
                "2*(3+4), 14",
                "((1+2)*(3+4)), 21",
                "(1+2)*(3+4)/7, 3",
                // The examples given in the assignment statement.
                "10/2+7, 12",
                "-5+8, 3",
                "3*-2, -6"
        })
        void respectsPrecedenceAndParentheses(String expression, String expected) {
            assertEvaluates(expression, expected);
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "-5, -5",
                "+5, 5",
                "--5, 5",
                "-(-5), 5",
                "-(3+4), -7",
                "3*-2, -6",
                "3*+2, 6",
                "-2*-3, 6"
        })
        void supportsUnaryPlusAndMinus(String expression, String expected) {
            assertEvaluates(expression, expected);
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1.5+2.25, 3.75",
                "0.1+0.2, 0.3",
                "2.5*4, 10",
                ".5+.5, 1",
                "3.0-1.5, 1.5",
                "(1/4)*4, 1",
                "0.3*3, 0.9"
        })
        void supportsDecimalNumbers(String expression, String expected) {
            // Exact decimal arithmetic: 0.1 + 0.2 must be 0.3, not 0.30000000000000004.
            assertEvaluates(expression, expected);
        }

        @Test
        @DisplayName("trailing zeros are removed from the result")
        void normalizesTrailingZeros() {
            assertEvaluates("5.00+0", "5");
            assertEvaluates("10/4", "2.5");
            assertEvaluates("1/8", "0.125");
        }

        @Test
        @DisplayName("very large numbers keep full precision instead of overflowing")
        void supportsLargeNumbers() {
            assertEvaluates("99999999999999999999+1", "100000000000000000000");
        }
    }

    @Nested
    @DisplayName("Function 2 - exception handling")
    class ExceptionHandling {

        @ParameterizedTest(name = "division by zero: {0}")
        @ValueSource(strings = {"1/0", "5/(3-3)", "0/0", "1/0.0"})
        void rejectsDivisionByZero(String expression) {
            assertFails(expression, ErrorCode.DIVISION_BY_ZERO);
        }

        @ParameterizedTest(name = "invalid expression: {0}")
        @ValueSource(strings = {
                "1+",
                "*5",
                "1++*2",
                "(1+2",
                "1+2)",
                "()",
                "abc+1",
                "1+2@3",
                "..",
                "5**2",
                "1+()",
                "1+2))",
                "2*/3"
        })
        void rejectsInvalidExpressions(String expression) {
            assertFails(expression, ErrorCode.INVALID_EXPRESSION);
        }

        @ParameterizedTest(name = "empty expression: {0}")
        @ValueSource(strings = {"", "   "})
        void rejectsEmptyExpressions(String expression) {
            assertFails(expression, ErrorCode.EMPTY_EXPRESSION);
        }

        @Test
        @DisplayName("a null expression is rejected instead of throwing NullPointerException")
        void rejectsNullExpression() {
            CalculationException ex = assertThrows(CalculationException.class,
                    () -> ExpressionCalculator.evaluate(null));
            assertEquals(ErrorCode.EMPTY_EXPRESSION, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Input normalization")
    class Normalization {

        @Test
        @DisplayName("display symbols and full width characters are accepted")
        void normalizesUserFriendlyInput() {
            assertEquals("6*7", ExpressionNormalizer.normalize("6×7"));
            assertEquals("6/7", ExpressionNormalizer.normalize("6÷7"));
            assertEquals("(1+2)*3", ExpressionNormalizer.normalize("（1＋2）＊3"));
            assertEquals("1+2", ExpressionNormalizer.normalize(" 1 + 2 "));
        }

        @Test
        @DisplayName("null becomes an empty string")
        void handlesNull() {
            assertEquals("", ExpressionNormalizer.normalize(null));
        }
    }
}
