package com.calcflow.service;

import com.calcflow.dto.CalculationRequest;
import com.calcflow.dto.CalculationResponse;
import com.calcflow.entity.CalculationHistory;
import com.calcflow.exception.BadRequestException;
import com.calcflow.repository.CalculationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CalculatorService {

    private static final MathContext MATH_CONTEXT = new MathContext(15, RoundingMode.HALF_UP);
    private final CalculationRepository calculationRepository;

    public CalculatorService(CalculationRepository calculationRepository) {
        this.calculationRepository = calculationRepository;
    }

    public CalculationResponse calculate(CalculationRequest request) {
        String expression = normalizeExpression(request.getExpression());
        BigDecimal result = evaluate(expression);
        String formattedResult = formatResult(result);

        CalculationHistory history = new CalculationHistory(expression, formattedResult);
        CalculationHistory saved = calculationRepository.save(history);
        return CalculationResponse.fromEntity(saved);
    }

    public List<CalculationResponse> getHistory() {
        return calculationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(CalculationResponse::fromEntity)
                .toList();
    }

    public CalculationResponse getById(Long id) {
        CalculationHistory record = calculationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calculation with id " + id + " was not found"));
        return CalculationResponse.fromEntity(record);
    }

    @Transactional
    public void clearHistory() {
        calculationRepository.deleteAll();
    }

    public String health() {
        return "OK";
    }

    private String normalizeExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new BadRequestException("Expression is required");
        }

        String normalized = expression.replace(" ", "")
                .replace("−", "-")
                .replace("×", "*")
                .replace("÷", "/");

        if (normalized.isEmpty()) {
            throw new BadRequestException("Expression is required");
        }
        if (normalized.length() > 500) {
            throw new BadRequestException("Expression length exceeds the maximum limit");
        }

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetter(ch)) {
                if (!normalized.regionMatches(true, i, "sqrt", 0, 4)) {
                    throw new BadRequestException("Expression contains unsupported characters");
                }
                i += 3;
                continue;
            }
            if (!(Character.isDigit(ch) || ch == '.' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '(' || ch == ')' || ch == '%' || ch == '^')) {
                throw new BadRequestException("Expression contains unsupported characters");
            }
        }

        return normalized;
    }

    private BigDecimal evaluate(String expression) {
        Parser parser = new Parser(expression, this);
        return parser.parseExpression();
    }

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        if (exponent.stripTrailingZeros().scale() > 0) {
            throw new BadRequestException("Exponent must be a whole number");
        }
        int exponentValue = exponent.intValueExact();
        if (exponentValue < 0) {
            if (base.compareTo(BigDecimal.ZERO) == 0) {
                throw new BadRequestException("Division by zero is not allowed");
            }
            return BigDecimal.ONE.divide(base.pow(-exponentValue), MATH_CONTEXT);
        }
        return base.pow(exponentValue, MATH_CONTEXT);
    }

    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Square root is not defined for negative numbers");
        }
        return BigDecimal.valueOf(Math.sqrt(value.doubleValue())).setScale(10, RoundingMode.HALF_UP);
    }

    private String formatResult(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.toPlainString();
    }

    private static final class Parser {
        private final String expression;
        private final CalculatorService calculatorService;
        private int index;

        private Parser(String expression, CalculatorService calculatorService) {
            this.expression = expression;
            this.calculatorService = calculatorService;
        }

        private BigDecimal parseExpression() {
            BigDecimal value = parseAddSubtract();
            skipWhitespace();
            if (index != expression.length()) {
                throw new BadRequestException("Malformed expression");
            }
            return value;
        }

        private BigDecimal parseAddSubtract() {
            BigDecimal value = parseMultiplyDivide();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    skipWhitespace();
                    if (current() == '+' || current() == '*' || current() == '/' || current() == '%' || current() == '^' || current() == ')' || current() == '\0') {
                        throw new BadRequestException("Malformed expression");
                    }
                    value = value.add(parseMultiplyDivide(), MATH_CONTEXT);
                } else if (match('-')) {
                    skipWhitespace();
                    if (current() == '*' || current() == '/' || current() == '%' || current() == '^' || current() == ')' || current() == '\0') {
                        throw new BadRequestException("Malformed expression");
                    }
                    value = value.subtract(parseMultiplyDivide(), MATH_CONTEXT);
                } else {
                    return value;
                }
            }
        }

        private BigDecimal parseMultiplyDivide() {
            BigDecimal value = parsePower();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    skipWhitespace();
                    if (current() == '+' || current() == '-' || current() == '*' || current() == '/' || current() == '%' || current() == '^' || current() == ')' || current() == '\0') {
                        throw new BadRequestException("Malformed expression");
                    }
                    value = value.multiply(parsePower(), MATH_CONTEXT);
                } else if (match('/')) {
                    skipWhitespace();
                    if (current() == '+' || current() == '-' || current() == '*' || current() == '/' || current() == '%' || current() == '^' || current() == ')' || current() == '\0') {
                        throw new BadRequestException("Malformed expression");
                    }
                    BigDecimal divisor = parsePower();
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw new BadRequestException("Division by zero is not allowed");
                    }
                    value = value.divide(divisor, MATH_CONTEXT);
                } else if (match('%')) {
                    skipWhitespace();
                    if (current() == '+' || current() == '-' || current() == '*' || current() == '/' || current() == '%' || current() == '^' || current() == ')' || current() == '\0') {
                        throw new BadRequestException("Malformed expression");
                    }
                    value = value.divide(BigDecimal.valueOf(100), MATH_CONTEXT);
                } else {
                    return value;
                }
            }
        }

        private BigDecimal parsePower() {
            BigDecimal value = parseUnary();
            skipWhitespace();
            if (match('^')) {
                BigDecimal exponent = parsePower();
                return calculatorService.pow(value, exponent);
            }
            return value;
        }

        private BigDecimal parseUnary() {
            skipWhitespace();
            if (match('-')) {
                if (current() == '+' || current() == '-') {
                    throw new BadRequestException("Malformed expression");
                }
                return parseUnary().negate();
            }
            if (match('+')) {
                if (current() == '+' || current() == '-') {
                    throw new BadRequestException("Malformed expression");
                }
                return parseUnary();
            }
            return parsePrimary();
        }

        private BigDecimal parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                BigDecimal value = parseAddSubtract();
                skipWhitespace();
                if (!match(')')) {
                    throw new BadRequestException("Mismatched parentheses");
                }
                return value;
            }

            if (startsWithKeyword("sqrt")) {
                index += 4;
                skipWhitespace();
                if (!match('(')) {
                    throw new BadRequestException("Malformed expression");
                }
                BigDecimal value = parseAddSubtract();
                skipWhitespace();
                if (!match(')')) {
                    throw new BadRequestException("Mismatched parentheses");
                }
                return calculatorService.sqrt(value);
            }

            if (current() == '.' || Character.isDigit(current())) {
                return parseNumber();
            }

            throw new BadRequestException("Malformed expression");
        }

        private BigDecimal parseNumber() {
            int start = index;
            while (index < expression.length() && (Character.isDigit(expression.charAt(index)) || expression.charAt(index) == '.')) {
                index++;
            }
            String numberText = expression.substring(start, index);
            if (numberText.indexOf('.') != numberText.lastIndexOf('.')) {
                throw new BadRequestException("Malformed expression");
            }
            try {
                return new BigDecimal(numberText);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Malformed expression");
            }
        }

        private boolean startsWithKeyword(String keyword) {
            return expression.regionMatches(true, index, keyword, 0, keyword.length());
        }

        private void skipWhitespace() {
            while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
        }

        private boolean match(char expected) {
            skipWhitespace();
            if (index < expression.length() && expression.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private char current() {
            if (index >= expression.length()) {
                return '\0';
            }
            return expression.charAt(index);
        }
    }
}
