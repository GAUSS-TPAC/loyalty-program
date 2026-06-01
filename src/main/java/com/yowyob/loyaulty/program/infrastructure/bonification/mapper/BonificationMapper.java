package com.yowyob.loyaulty.program.infrastructure.bonification.mapper;

import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationReward;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationHistoryItemDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationRewardDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationTransactionRequestDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationTransactionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface BonificationMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "externalUserId", source = "userId")
    @Mapping(target = "occurredAt", expression = "java(parseInstant(dto.createdAt()))")
    BonificationTransaction toTransaction(BonificationTransactionResponseDto dto);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "externalUserId", source = "userId")
    @Mapping(target = "occurredAt", expression = "java(parseInstant(dto.createdAt()))")
    BonificationTransaction toTransactionFromHistory(BonificationHistoryItemDto dto);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "rewardTriggered", expression = "java(dto.rewardGranted() != null)")
    @Mapping(target = "triggeredReward", source = "rewardGranted")
    PointsResult toPointsResult(BonificationTransactionResponseDto dto);

    @Mapping(target = "rewardId", source = "id")
    BonificationReward toReward(BonificationRewardDto dto);

    default BonificationTransactionRequestDto toRequestDto(String userId,
                                                            BigDecimal amount,
                                                            String description) {
        return new BonificationTransactionRequestDto(userId, amount, description);
    }

    default Instant parseInstant(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(createdAt);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
