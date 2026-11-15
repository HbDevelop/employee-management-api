package com.example.employeeapi.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import com.example.employeeapi.core.config.AppProperties;
import com.example.employeeapi.users.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String TEST_SECRET = "unit-test-secret-unit-test-secret-0123456789";

    private static JwtService jwtService(String secret, Duration ttl) {
        return new JwtService(new AppProperties(4, secret, ttl));
    }

    private static User user() {
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        user.setId("66b0c0ffee0000000000abcd");
        return user;
    }

    @Test
    void generatedTokenCanBeVerifiedAndCarriesTheUser() {
        JwtService service = jwtService(TEST_SECRET, Duration.ofDays(1));

        AuthUser authUser = service.verifyToken(service.generateToken(user()));

        assertThat(authUser).isEqualTo(new AuthUser("66b0c0ffee0000000000abcd", "ada@example.com", "Ada", "Lovelace"));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = jwtService(TEST_SECRET, Duration.ofSeconds(-60));
        String token = service.generateToken(user());

        assertThatThrownBy(() -> service.verifyToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = jwtService("another-secret-another-secret-9876543210", Duration.ofDays(1)).generateToken(user());
        JwtService service = jwtService(TEST_SECRET, Duration.ofDays(1));

        assertThatThrownBy(() -> service.verifyToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void malformedTokenIsRejected() {
        JwtService service = jwtService(TEST_SECRET, Duration.ofDays(1));

        assertThatThrownBy(() -> service.verifyToken("not-a-token")).isInstanceOf(JwtException.class);
    }

    @Test
    void tooShortSecretIsRefusedAtStartup() {
        assertThatThrownBy(() -> jwtService("short", Duration.ofDays(1))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jwtService(null, Duration.ofDays(1))).isInstanceOf(IllegalStateException.class);
    }
}
