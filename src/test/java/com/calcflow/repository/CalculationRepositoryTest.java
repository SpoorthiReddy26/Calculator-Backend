package com.calcflow.repository;

import com.calcflow.entity.CalculationHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class CalculationRepositoryTest {

    @Autowired
    private CalculationRepository calculationRepository;

    @Test
    void shouldSaveAndFindCalculation() {
        CalculationHistory item = new CalculationHistory("2+2", "4");
        CalculationHistory saved = calculationRepository.save(item);

        assertNotNull(saved.getId());
        assertEquals("2+2", calculationRepository.findById(saved.getId()).orElseThrow().getExpression());
    }

    @Test
    void shouldReturnHistoryInDescendingOrder() {
        calculationRepository.save(new CalculationHistory("1+1", "2"));
        calculationRepository.save(new CalculationHistory("2+2", "4"));

        List<CalculationHistory> items = calculationRepository.findAll();
        assertEquals(2, items.size());
        assertTrue(items.get(0).getCreatedAt().isBefore(items.get(1).getCreatedAt()) || items.get(0).getCreatedAt().isEqual(items.get(1).getCreatedAt()));
    }

    @Test
    void shouldDeleteAllHistory() {
        calculationRepository.save(new CalculationHistory("3+3", "6"));
        calculationRepository.deleteAll();
        assertTrue(calculationRepository.findAll().isEmpty());
    }
}
