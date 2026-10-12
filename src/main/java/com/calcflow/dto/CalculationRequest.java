package com.calcflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CalculationRequest {

    @NotBlank(message = "Expression is required")
    @Size(max = 500, message = "Expression must be at most 500 characters")
    private String expression;

    public CalculationRequest() {
    }

    public CalculationRequest(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
