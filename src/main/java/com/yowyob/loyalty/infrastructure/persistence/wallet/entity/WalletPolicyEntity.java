package com.yowyob.loyalty.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_policies")
public class WalletPolicyEntity {
    @Id
    private UUID id;
    
    @Column("tenant_id")
    private UUID tenantId;
    
    @Column("currency_name")
    private String currencyName;
    
    @Column("currency_symbol")
    private String currencySymbol;
    
    @Column("exchange_rate")
    private BigDecimal exchangeRate;
    
    @Column("daily_spend_cap")
    private BigDecimal dailySpendCap;
    
    @Column("max_balance")
    private BigDecimal maxBalance;
    
    @Column("max_topup_per_txn")
    private BigDecimal maxTopupPerTxn;
    
    @Column("min_withdrawal")
    private BigDecimal minWithdrawal;
    
    @Column("withdrawal_delay_hours")
    private Integer withdrawalDelayHours;
    
    @Column("otp_threshold")
    private BigDecimal otpThreshold;
    
    @Column("kyc_required")
    private Boolean kycRequired;
    
    @Column("expiry_days")
    private Integer expiryDays;
}
