package com.fzu.calculator.repository;

import com.fzu.calculator.entity.CalculationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the calculation history table.
 */
@Repository
public interface CalculationHistoryRepository extends JpaRepository<CalculationHistory, Long> {
}
