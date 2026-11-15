package com.example.employeeapi.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Réglages de sécurité, lus dans les variables d'environnement. */
@ConfigurationProperties(prefix = "app.security")
public record AppProperties(int bcryptStrength, String jwtSecret, Duration jwtTtl) {
}
