package com.legistrack.app.controller;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

final class CurrentUser {
    private CurrentUser() {}

    static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
