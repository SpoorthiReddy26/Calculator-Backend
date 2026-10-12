package com.calcflow.repository;

import com.calcflow.entity.CalculationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculationRepository extends JpaRepository<CalculationHistory, Long> {
}
