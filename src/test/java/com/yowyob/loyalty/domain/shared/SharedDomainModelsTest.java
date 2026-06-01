package com.yowyob.loyalty.domain.shared;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import com.yowyob.loyalty.domain.shared.model.Money;
import com.yowyob.loyalty.domain.shared.model.PageRequest;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class SharedDomainModelsTest {

    @Test
    public void testInvalidTenantIdThrowsException() {
        assertThrows(DomainValidationException.class, () -> {
            TenantId.of("invalid");
        });
    }

    @Test
    public void testNegativeMoneyThrowsException() {
        assertThrows(DomainValidationException.class, () -> {
            Money.of(new BigDecimal("-1"), "XAF");
        });
    }

    @Test
    public void testMoneyAddDifferentCurrenciesThrowsException() {
        Money moneyXaf = Money.of(new BigDecimal("100"), "XAF");
        Money moneyEur = Money.of(new BigDecimal("50"), "EUR");

        assertThrows(DomainValidationException.class, () -> {
            moneyXaf.add(moneyEur);
        });
    }

    @Test
    public void testInvalidPageRequestThrowsException() {
        assertThrows(DomainValidationException.class, () -> {
            PageRequest.of(-1, 10);
        });
    }

    @Test
    public void testValidMoneyOperations() {
        Money m1 = Money.of(new BigDecimal("100"), "XAF");
        Money m2 = Money.of(new BigDecimal("50"), "XAF");
        
        assertEquals(new BigDecimal("150"), m1.add(m2).amount());
        assertEquals(new BigDecimal("50"), m1.subtract(m2).amount());
    }
}
