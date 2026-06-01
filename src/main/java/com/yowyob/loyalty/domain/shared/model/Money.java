package com.yowyob.loyalty.domain.shared.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null) {
            throw new DomainValidationException("amount ne doit pas être null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("amount ne doit pas être négatif");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainValidationException("currency ne doit pas être vide ou null");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public Money add(Money other) {
        if (other == null) {
            throw new DomainValidationException("L'autre montant ne doit pas être null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Devises différentes : " + this.currency + " et " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new DomainValidationException("L'autre montant ne doit pas être null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Devises différentes : " + this.currency + " et " + other.currency);
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Le résultat de la soustraction ne doit pas être négatif");
        }
        return new Money(result, this.currency);
    }

    public boolean isGreaterThan(Money other) {
        if (other == null) {
            throw new DomainValidationException("L'autre montant ne doit pas être null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Devises différentes : " + this.currency + " et " + other.currency);
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        if (other == null) {
            throw new DomainValidationException("L'autre montant ne doit pas être null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Devises différentes : " + this.currency + " et " + other.currency);
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
