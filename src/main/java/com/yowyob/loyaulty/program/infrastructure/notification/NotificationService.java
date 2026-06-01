package com.yowyob.loyaulty.program.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Dispatche les notifications vers les canaux configurés (FCM, SMS, email).
 * Les adapters réels (FCM, Twilio, SMTP) seront branchés ici quand les credentials
 * seront disponibles. En attendant, les messages sont loggés.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public Mono<Void> sendToMember(String tenantId, String memberId,
                                    String eventType, String message) {
        log.info("[NOTIFICATION] tenant={} member={} event={} message={}",
                tenantId, memberId, eventType, message);
        return Mono.empty();
    }

    public Mono<Void> sendToAdmin(String tenantId, String eventType, String message) {
        log.warn("[ADMIN-NOTIFICATION] tenant={} event={} message={}",
                tenantId, eventType, message);
        return Mono.empty();
    }
}
