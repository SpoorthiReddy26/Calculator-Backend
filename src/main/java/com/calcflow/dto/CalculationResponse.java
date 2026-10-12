package com.calcflow.dto;

import com.calcflow.entity.CalculationHistory;

import java.time.LocalDateTime;

public class CalculationResponse {
    private Long id;
    private String expression;
    private String result;
    private LocalDateTime createdAt;

    public CalculationResponse() {
    }

    public CalculationResponse(Long id, String expression, String result, LocalDateTime createdAt) {
        this.id = id;
        this.expression = expression;
        this.result = result;
        this.createdAt = createdAt;
    }

    public static CalculationResponse fromEntity(CalculationHistory entity) {
        return new CalculationResponse(
                entity.getId(),
                entity.getExpression(),
                entity.getResult(),
                entity.getCreatedAt()
        );
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
