package com.yowyob.loyalty.domain.loyalty.model.points;

import java.util.UUID;

/**
 * Flux de points agrégé par clé API : total crédité et débité par les
 * événements soumis avec cette clé (attribution via metadata.api_key_id
 * des transactions de points).
 */
public record ApiKeyPointsFlow(
        UUID apiKeyId,
        long credited,
        long debited
) {}
