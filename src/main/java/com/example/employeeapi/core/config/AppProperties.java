package com.example.employeeapi.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Équivalent du ConfigService Express : valeurs lues depuis les variables d'environnement. */
@ConfigurationProperties(prefix = "app.security")
public record AppProperties(int bcryptStrength, String jwtSecret, Duration jwtTtl) {
}
