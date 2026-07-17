package com.yowyob.loyalty.api.auth;

import com.yowyob.loyalty.api.auth.dto.ConfirmMfaRequest;
import com.yowyob.loyalty.api.auth.dto.LoginRequest;
import com.yowyob.loyalty.api.auth.dto.LoginResponse;
import com.yowyob.loyalty.application.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentification des administrateurs de tenant (portail admin)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion admin", description = "Authentifie un administrateur par email/mot de passe via KernelCore. "
            + "Si le compte a le MFA actif, retourne mfaRequired=true + mfaToken (code envoyé par email, "
            + "à confirmer via /login/mfa) ; sinon retourne directement le JWT et l'organisation active "
            + "(à renvoyer via X-Organization-Id).")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password(), request.organizationId())
                .map(LoginResponse::from);
    }

    @PostMapping("/login/mfa")
    @Operation(summary = "Confirmation MFA", description = "Deuxième étape du login : confirme le code reçu par email "
            + "avec le mfaToken renvoyé par /login, et retourne le JWT ainsi que l'organisation active.")
    public Mono<LoginResponse> confirmMfa(@Valid @RequestBody ConfirmMfaRequest request) {
        return authService.confirmMfa(request.mfaToken(), request.code(), request.organizationId())
                .map(LoginResponse::new);
    }
}
