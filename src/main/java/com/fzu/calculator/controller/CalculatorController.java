package com.fzu.calculator.controller;

import com.fzu.calculator.dto.ApiMessageResponse;
import com.fzu.calculator.dto.CalculateRequest;
import com.fzu.calculator.dto.CalculateResponse;
import com.fzu.calculator.dto.HistoryListResponse;
import com.fzu.calculator.service.CalculationService;
import com.fzu.calculator.service.HistoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API of the calculator system.
 *
 * <pre>
 * GET    /api/health          liveness probe used by the deployment check
 * POST   /api/calculate       evaluate an expression and persist the result
 * GET    /api/history         the stored history, newest first
 * DELETE /api/history/{id}    delete one history record
 * </pre>
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CalculatorController {

    private final CalculationService calculationService;
    private final HistoryService historyService;

    public CalculatorController(CalculationService calculationService, HistoryService historyService) {
        this.calculationService = calculationService;
        this.historyService = historyService;
    }

    @PostMapping(value = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CalculateResponse calculate(@RequestBody CalculateRequest request) {
        String expression = request == null ? null : request.getExpression();
        return calculationService.calculate(expression);
    }

    @GetMapping("/history")
    public HistoryListResponse history() {
        return historyService.findAll();
    }

    @DeleteMapping("/history/{id}")
    public ApiMessageResponse delete(@PathVariable("id") Long id) {
        return ApiMessageResponse.deleted(historyService.delete(id));
    }

    @GetMapping("/health")
    public java.util.Map<String, Object> health() {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<String, Object>();
        body.put("success", true);
        body.put("status", "UP");
        body.put("service", "calculator-backend");
        return body;
    }
}
