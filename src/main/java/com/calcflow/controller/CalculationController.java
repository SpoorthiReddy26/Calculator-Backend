package com.calcflow.controller;

import com.calcflow.dto.CalculationRequest;
import com.calcflow.dto.CalculationResponse;
import com.calcflow.service.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Calculations", description = "Calculator and history endpoints")
public class CalculationController {

    private final CalculatorService calculatorService;

    public CalculationController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(summary = "Evaluate an expression and save successful calculation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Calculation saved successfully",
                    content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or malformed expression"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/calculations")
    public ResponseEntity<CalculationResponse> calculate(@Valid @RequestBody CalculationRequest request) {
        CalculationResponse response = calculatorService.calculate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get recent calculation history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/calculations")
    public List<CalculationResponse> getHistory() {
        return calculatorService.getHistory();
    }

    @Operation(summary = "Get a calculation by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calculation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Calculation ID not found")
    })
    @GetMapping("/calculations/{id}")
    public CalculationResponse getById(@PathVariable Long id) {
        return calculatorService.getById(id);
    }

    @Operation(summary = "Clear all calculation history")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "History cleared successfully"),
            @ApiResponse(responseCode = "500", description = "Database error")
    })
    @DeleteMapping("/calculations")
    public ResponseEntity<Void> clearHistory() {
        calculatorService.clearHistory();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(calculatorService.health());
    }
}
