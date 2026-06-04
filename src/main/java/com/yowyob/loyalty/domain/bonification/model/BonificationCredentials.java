package com.yowyob.loyalty.domain.bonification.model;

public record BonificationCredentials(String login, String password) {
    public boolean isConfigured() {
        return login != null && !login.isBlank() && password != null && !password.isBlank();
    }
}
