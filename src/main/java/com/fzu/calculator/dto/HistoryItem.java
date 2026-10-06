package com.fzu.calculator.dto;

import java.math.BigDecimal;

/**
 * A single calculation history record returned by {@code GET /api/history}.
 */
public class HistoryItem {

    private Long id;
    private String expression;
    private BigDecimal result;
    private String createdAt;

    public HistoryItem(Long id, String expression, BigDecimal result, String createdAt) {
        this.id = id;
        this.expression = expression;
        this.result = result;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public BigDecimal getResult() {
        return result;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
