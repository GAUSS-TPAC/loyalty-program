package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Correspondance des points : 1 point de fidélité vaut {@code exchangeRate}
 * unités de la monnaie du tenant, affichée sous {@code currencyName}/{@code currencySymbol}.
 */
public record UpdatePointsConversionRequest(
    @NotBlank @Size(max = 50) String currencyName,
    @NotBlank @Size(max = 10) String currencySymbol,
    @NotNull @DecimalMin(value = "0.000001") BigDecimal exchangeRate
) {}
