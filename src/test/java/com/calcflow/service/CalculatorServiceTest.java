package com.calcflow.service;

import com.calcflow.dto.CalculationRequest;
import com.calcflow.entity.CalculationHistory;
import com.calcflow.exception.BadRequestException;
import com.calcflow.repository.CalculationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {

    @Mock
    private CalculationRepository calculationRepository;

    @InjectMocks
    private CalculatorService calculatorService;

    @Test
    void shouldAddTwoNumbers() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("2+3"));
        assertEquals("5", response.getResult());
    }

    @Test
    void shouldRespectOperatorPrecedence() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("2 + 3 * 4"));
        assertEquals("14", response.getResult());
    }

    @Test
    void shouldHandleDecimalArithmetic() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("1.5 + 2.25"));
        assertEquals("3.75", response.getResult());
    }

    @Test
    void shouldHandleParentheses() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("(2+3)*4"));
        assertEquals("20", response.getResult());
    }

    @Test
    void shouldHandleSquareRoot() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("sqrt(9)"));
        assertEquals("3", response.getResult());
    }

    @Test
    void shouldHandleExponentiation() {
        when(calculationRepository.save(any(CalculationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = calculatorService.calculate(new CalculationRequest("2^3"));
        assertEquals("8", response.getResult());
    }

    @Test
    void shouldThrowOnDivisionByZero() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> calculatorService.calculate(new CalculationRequest("5/0")));
        assertTrue(exception.getMessage().contains("Division by zero"));
    }

    @Test
    void shouldThrowOnInvalidSqrtInput() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> calculatorService.calculate(new CalculationRequest("sqrt(-9)")));
        assertTrue(exception.getMessage().contains("Square root"));
    }

    @Test
    void shouldThrowOnMalformedExpression() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> calculatorService.calculate(new CalculationRequest("2++3")));
        assertTrue(exception.getMessage().contains("Malformed") || exception.getMessage().contains("unsupported"));
    }

    @Test
    void shouldReturnHistory() {
        CalculationHistory c1 = new CalculationHistory("2+2", "4");
        CalculationHistory c2 = new CalculationHistory("3+3", "6");
        when(calculationRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(c1, c2));

        var history = calculatorService.getHistory();
        assertEquals(2, history.size());
        assertEquals("2+2", history.get(0).getExpression());
    }

    @Test
    void shouldGetCalculationById() {
        CalculationHistory record = new CalculationHistory("1+1", "2");
        record = new CalculationHistory("1+1", "2");
        when(calculationRepository.findById(1L)).thenReturn(Optional.of(record));

        var response = calculatorService.getById(1L);
        assertEquals("1+1", response.getExpression());
    }
}
