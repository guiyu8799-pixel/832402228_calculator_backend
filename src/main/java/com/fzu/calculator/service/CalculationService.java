package com.fzu.calculator.service;

import com.fzu.calculator.dto.CalculateResponse;
import com.fzu.calculator.entity.CalculationHistory;
import com.fzu.calculator.exception.CalculationException;
import com.fzu.calculator.exception.ErrorCode;
import com.fzu.calculator.expression.ExpressionCalculator;
import com.fzu.calculator.expression.ExpressionNormalizer;
import com.fzu.calculator.repository.CalculationHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates, evaluates and persists a calculation.
 *
 * <p>This is the only place where a calculation result is produced; the front end never computes a
 * result on its own.</p>
 */
@Service
public class CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CalculationHistoryRepository repository;

    public CalculationService(CalculationHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Evaluates a raw user expression and stores the successful result in the database.
     *
     * @param rawExpression expression exactly as typed by the user
     * @return the calculation result together with the persisted history id
     */
    @Transactional
    public CalculateResponse calculate(String rawExpression) {
        String canonicalExpression = ExpressionNormalizer.normalize(rawExpression);
        BigDecimal result = ExpressionCalculator.evaluate(canonicalExpression);
        CalculationHistory saved = repository.save(
                new CalculationHistory(canonicalExpression, result.toPlainString(), LocalDateTime.now()));
        log.info("Calculated '{}' = {} (history id {})", canonicalExpression, result.toPlainString(),
                saved.getId());
        return new CalculateResponse(canonicalExpression, result, saved.getId(),
                saved.getCreatedAt().format(FORMATTER));
    }
}
