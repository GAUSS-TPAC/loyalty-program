package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("loyalty_counters")
public class CounterEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private String counterKey;
    private long value;
    private String windowType;
    private Instant windowStart;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public String getCounterKey() { return counterKey; }
    public void setCounterKey(String counterKey) { this.counterKey = counterKey; }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    public String getWindowType() { return windowType; }
    public void setWindowType(String windowType) { this.windowType = windowType; }
    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
