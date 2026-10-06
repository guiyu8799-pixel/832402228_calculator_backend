package com.fzu.calculator.dto;

import java.math.BigDecimal;

/**
 * Successful response of {@code POST /api/calculate}.
 */
public class CalculateResponse {

    private final boolean success = true;
    private String expression;
    private BigDecimal result;
    private Long id;
    private String createdAt;

    public CalculateResponse(String expression, BigDecimal result, Long id, String createdAt) {
        this.expression = expression;
        this.result = result;
        this.id = id;
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getExpression() {
        return expression;
    }

    public BigDecimal getResult() {
        return result;
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
