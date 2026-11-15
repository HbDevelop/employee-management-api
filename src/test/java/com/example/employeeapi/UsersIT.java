package com.example.employeeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Teste les routes des utilisateurs de bout en bout. */
class UsersIT extends AbstractIntegrationTest {

    @Test
    void returns401WithoutToken() {
        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/users/me", null, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meReturnsTheConnectedUserWithoutThePassword() {
        String token = registerAndLogin("ada@example.com");

        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/users/me", token, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("email", "ada@example.com")
                .containsEntry("firstName", "Ada")
                .containsEntry("lastName", "Lovelace")
                .containsKey("_id")
                .doesNotContainKey("password");
    }

    @Test
    void aUserCanBeReadById() {
        String otherUserId = register("grace@example.com");
        String token = registerAndLogin("ada@example.com");

        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/users/" + otherUserId, token, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("_id", otherUserId)
                .containsEntry("email", "grace@example.com")
                .doesNotContainKey("password");
    }

    @Test
    void returns404ForAnUnknownOrMalformedId() {
        String token = registerAndLogin("ada@example.com");

        assertThat(exchange(HttpMethod.GET, "/users/" + new ObjectId().toHexString(), token, null, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange(HttpMethod.GET, "/users/not-an-id", token, null, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
