package com.fzu.calculator.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * One successfully evaluated calculation.
 *
 * <p>Table {@code calculation_history}</p>
 * <pre>
 * id          BIGINT  PRIMARY KEY AUTOINCREMENT
 * expression  VARCHAR(512)  the canonical expression, for example "(1+2)*3"
 * result      VARCHAR(128)  the result kept as plain decimal text so that no precision is lost
 * created_at  TIMESTAMP     creation time
 * </pre>
 */
@Entity
@Table(name = "calculation_history", indexes = {
        @Index(name = "idx_history_created_at", columnList = "created_at")
})
public class CalculationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "expression", nullable = false, length = 512)
    private String expression;

    @Column(name = "result", nullable = false, length = 128)
    private String result;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CalculationHistory() {
    }

    public CalculationHistory(String expression, String result, LocalDateTime createdAt) {
        this.expression = expression;
        this.result = result;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
