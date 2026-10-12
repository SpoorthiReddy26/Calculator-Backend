package com.calcflow.controller;

import com.calcflow.dto.CalculationRequest;
import com.calcflow.dto.CalculationResponse;
import com.calcflow.service.CalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculationController.class)
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalculatorService calculatorService;

    @Test
    void shouldHandleCalculationRequest() throws Exception {
        CalculationResponse response = new CalculationResponse(1L, "2+2", "4", LocalDateTime.now());
        Mockito.when(calculatorService.calculate(any(CalculationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"2+2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("4"))
                .andExpect(jsonPath("$.expression").value("2+2"));
    }

    @Test
    void shouldReturnHistory() throws Exception {
        CalculationResponse response = new CalculationResponse(1L, "2+2", "4", LocalDateTime.now());
        Mockito.when(calculatorService.getHistory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].result").value("4"));
    }

    @Test
    void shouldReturnCalculationById() throws Exception {
        CalculationResponse response = new CalculationResponse(1L, "2+2", "4", LocalDateTime.now());
        Mockito.when(calculatorService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/calculations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteHistory() throws Exception {
        mockMvc.perform(delete("/api/v1/calculations"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldHandleValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
