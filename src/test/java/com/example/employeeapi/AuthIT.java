package com.example.employeeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.example.employeeapi.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Teste l'inscription et la connexion de bout en bout. */
class AuthIT extends AbstractIntegrationTest {

    private static final String EMAIL = "ada@example.com";

    // ---------- /auth/register ----------

    @Test
    @SuppressWarnings("unchecked")
    void registerReturnsErrorsWhenRequiredFieldsAreNotDefined() {
        ResponseEntity<List> response = rest.postForEntity("/auth/register", null, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<Map<String, String>>) response.getBody()).containsExactly(
                Map.of("field", "email", "error", "The user email is required"),
                Map.of("field", "password", "error", "The user password is required"),
                Map.of("field", "firstName", "error", "The user firstName is required"),
                Map.of("field", "lastName", "error", "The user lastName is required"));
        assertThat(userRepository.count()).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void registerReturnsErrorsWhenEmailAndPasswordAreNotValid() {
        Map<String, Object> body = Map.of(
                "email", "anonymousexample.com", "password", "A68u9", "firstName", "Anonymous", "lastName", "A.");

        ResponseEntity<List> response = rest.postForEntity("/auth/register", body, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<Map<String, String>>) response.getBody())
                .extracting(error -> error.get("field"))
                .containsExactly("email", "password");
    }

    @Test
    void registerCreatesTheUserWithAHashedPassword() {
        ResponseEntity<Map> response = rest.postForEntity("/auth/register", registration(EMAIL), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .containsEntry("email", EMAIL)
                .containsEntry("firstName", "Ada")
                .containsEntry("lastName", "Lovelace")
                .containsKey("_id")
                .doesNotContainKey("password");

        User stored = userRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(stored.getId()).isEqualTo(response.getBody().get("_id"));
        assertThat(stored.getPassword()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(stored.isActive()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void registerRejectsAnAlreadyUsedEmail() {
        register(EMAIL);

        ResponseEntity<List> response = rest.postForEntity("/auth/register", registration(EMAIL), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<Map<String, Object>>) response.getBody()).containsExactly(Map.of(
                "field", "email",
                "errors", List.of("Duplicate constraint violation. The value '" + EMAIL + "' is already used!!")));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    // ---------- /auth/login ----------

    @Test
    @SuppressWarnings("unchecked")
    void loginReturnsErrorsWhenRequiredFieldsAreNotDefined() {
        ResponseEntity<List> response = rest.postForEntity("/auth/login", null, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<Map<String, String>>) response.getBody()).containsExactly(
                Map.of("field", "email", "error", "The user email is required"),
                Map.of("field", "password", "error", "The user password is required"));
    }

    @Test
    void loginReturns401WhenTheUserDoesNotExist() {
        ResponseEntity<Map> response = rest.postForEntity(
                "/auth/login", Map.of("email", "unknown@example.com", "password", PASSWORD), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid credentials");
    }

    @Test
    void loginReturns401WhenThePasswordIsWrong() {
        register(EMAIL);

        ResponseEntity<Map> response = rest.postForEntity(
                "/auth/login", Map.of("email", EMAIL, "password", "Wr0ng-Password!"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid credentials");
    }

    @Test
    void loginReturnsATokenWhenCredentialsAreValid() {
        register(EMAIL);

        ResponseEntity<Map> response = rest.postForEntity(
                "/auth/login", Map.of("email", EMAIL, "password", PASSWORD), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) response.getBody().get("token")).matches("[\\w-]+\\.[\\w-]+\\.[\\w-]+");
    }
}
