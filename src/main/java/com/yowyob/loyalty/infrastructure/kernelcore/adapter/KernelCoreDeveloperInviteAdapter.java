package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelActorDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelAssignRoleRequest;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelCreateActorRequest;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelCreateRoleRequest;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelForgotPasswordRequest;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelRegisterUserRequest;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelRoleDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelUserAccountDto;
import com.yowyob.loyalty.shared.exception.DeveloperInviteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Orchestre, côté Kernel Core, la création d'un compte développeur pour le tenant courant :
 * acteur -> compte utilisateur (sans mot de passe) -> rôle "developer" (créé une fois par tenant,
 * réutilisé ensuite) -> assignation -> email Kernel Core de définition de mot de passe.
 * Ces endpoints d'administration Kernel Core exigent le Bearer JWT de l'admin appelant en plus
 * des headers X-Client-Id/X-Api-Key déjà portés par défaut par kernelCoreWebClient (vérifié en
 * conditions réelles : 403 ACCESS_DENIED sans ce Bearer, voir KernelCoreProfileController pour
 * le même pattern de transmission).
 */
public class KernelCoreDeveloperInviteAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreDeveloperInviteAdapter.class);
    private static final String DEVELOPER_ROLE_CODE = "developer";

    private static final ParameterizedTypeReference<KernelApiResponse<KernelActorDto>> ACTOR_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelUserAccountDto>> USER_ACCOUNT_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<KernelRoleDto>> ROLE_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelRoleDto>> ROLE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient kernelCoreWebClient;

    public KernelCoreDeveloperInviteAdapter(WebClient kernelCoreWebClient) {
        this.kernelCoreWebClient = kernelCoreWebClient;
    }

    public Mono<UUID> createActor(String bearerToken, UUID organizationId, String firstName, String lastName, String email) {
        return kernelCoreWebClient.post()
                .uri("/api/actors")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelCreateActorRequest(organizationId, firstName, lastName, email))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(ACTOR_TYPE)
                .flatMap(response -> unwrap(response, "création de l'acteur"))
                .map(KernelActorDto::getId);
    }

    public Mono<UUID> registerUser(String bearerToken, UUID actorId, String email) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/register")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelRegisterUserRequest(actorId, email, email, "LOCAL"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(USER_ACCOUNT_TYPE)
                .flatMap(response -> unwrap(response, "création du compte utilisateur"))
                .map(KernelUserAccountDto::getId);
    }

    /** Rôle "developer" partagé, créé une seule fois par tenant puis réutilisé pour chaque invitation suivante. */
    public Mono<UUID> findOrCreateDeveloperRole(String bearerToken) {
        return kernelCoreWebClient.get()
                .uri("/api/roles")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(ROLE_LIST_TYPE)
                .flatMap(roles -> roles.stream()
                        .filter(r -> DEVELOPER_ROLE_CODE.equals(r.getCode()))
                        .findFirst()
                        .map(r -> Mono.just(r.getId()))
                        .orElseGet(() -> createDeveloperRole(bearerToken)));
    }

    private Mono<UUID> createDeveloperRole(String bearerToken) {
        return kernelCoreWebClient.post()
                .uri("/api/roles")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelCreateRoleRequest(DEVELOPER_ROLE_CODE, "Développeur", "ORGANIZATION", List.of()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(ROLE_TYPE)
                .flatMap(response -> unwrap(response, "création du rôle développeur"))
                .map(KernelRoleDto::getId);
    }

    public Mono<Void> assignRole(String bearerToken, UUID userId, UUID roleId, UUID organizationId) {
        return kernelCoreWebClient.post()
                .uri("/api/roles/assignments")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelAssignRoleRequest(userId, roleId, "ORGANIZATION", organizationId, "ORGANIZATION"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(Void.class)
                .then();
    }

    public Mono<Void> sendPasswordSetupEmail(String bearerToken, String email) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/forgot-password")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelForgotPasswordRequest(email))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toDeveloperInviteException)
                .bodyToMono(Void.class)
                .then();
    }

    private <T> Mono<T> unwrap(KernelApiResponse<T> response, String step) {
        if (!response.isSuccess() || response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "réponse Kernel Core invalide";
            return Mono.error(new DeveloperInviteException("Échec Kernel Core (" + step + "): " + message));
        }
        return Mono.just(response.getData());
    }

    private Mono<? extends Throwable> toDeveloperInviteException(org.springframework.web.reactive.function.client.ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    log.warn("Échec appel Kernel Core (invitation développeur) [{}]: {}", resp.statusCode(), body);
                    return Mono.error(new DeveloperInviteException(
                            "Kernel Core a refusé l'opération (" + resp.statusCode() + "): " + body));
                });
    }
}
