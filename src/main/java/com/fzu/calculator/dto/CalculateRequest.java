package com.fzu.calculator.dto;

/**
 * Request body of {@code POST /api/calculate}, for example {@code {"expression": "(1+2)*3"}}.
 */
public class CalculateRequest {

    private String expression;

    public CalculateRequest() {
    }

    public CalculateRequest(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
