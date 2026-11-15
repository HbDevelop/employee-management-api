package com.example.employeeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.example.employeeapi.authentication.JwtService;
import com.example.employeeapi.core.config.AppProperties;
import com.example.employeeapi.users.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Équivalent de test/access-protected-ressource.test.ts, étendu au CRUD des employés. */
class EmployeesIT extends AbstractIntegrationTest {

    private static final Map<String, String> ADA = Map.of("department", "IT", "name", "Ada", "level", "S");

    private String createEmployee(String token, Map<String, String> employee) {
        ResponseEntity<Map> response = exchange(HttpMethod.POST, "/employees", token, employee, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (String) response.getBody().get("_id");
    }

    // ---------- Accès à une ressource protégée ----------

    @Test
    void returns401WhenTheAuthorizationHeaderIsMissing() {
        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/employees", null, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error",
                "Invalid Authorization header. Expected format is : 'Authorization: Bearer <token>'");
    }

    @Test
    void returns401WhenTheTokenIsInvalid() {
        String token = registerAndLogin("ada@example.com");

        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/employees", token + "x", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid Token");
    }

    @Test
    void returns401WhenTheTokenIsExpired() {
        // Token signé avec le bon secret, mais déjà expiré
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        user.setId(new ObjectId().toHexString());
        String expiredToken = new JwtService(new AppProperties(4, JWT_SECRET, Duration.ofMinutes(-5)))
                .generateToken(user);

        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/employees", expiredToken, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Token expired");
    }

    @Test
    void returnsTheEmployeesWhenTheTokenIsValid() {
        String token = registerAndLogin("ada@example.com");

        ResponseEntity<List> response = exchange(HttpMethod.GET, "/employees", token, null, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // ---------- CRUD des employés ----------

    @Test
    @SuppressWarnings("unchecked")
    void createEmployeeRejectsInvalidData() {
        String token = registerAndLogin("ada@example.com");

        ResponseEntity<List> response = exchange(
                HttpMethod.POST, "/employees", token, Map.of("department", "Sales", "name", "Ada"), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<Map<String, String>>) response.getBody())
                .extracting(error -> error.get("field"))
                .containsExactly("department", "level");
        assertThat(employeeRepository.count()).isZero();
    }

    @Test
    void createdEmployeeIsStoredWithItsCreatorAndCanBeReadBack() {
        String creatorId = register("ada@example.com");
        String token = registerAndLogin("grace@example.com");
        String graceId = userRepository.findByEmail("grace@example.com").orElseThrow().getId();

        String employeeId = createEmployee(token, ADA);

        ResponseEntity<Map> response = exchange(HttpMethod.GET, "/employees/" + employeeId, token, null, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("_id", employeeId)
                .containsEntry("name", "Ada")
                .containsEntry("department", "IT")
                .containsEntry("level", "S")
                .containsEntry("createdBy", graceId);
        assertThat(graceId).isNotEqualTo(creatorId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void employeesCanBeFilteredByDepartment() {
        String token = registerAndLogin("ada@example.com");
        createEmployee(token, ADA);
        createEmployee(token, Map.of("department", "HR", "name", "Bob", "level", "J"));

        ResponseEntity<List> all = exchange(HttpMethod.GET, "/employees", token, null, List.class);
        ResponseEntity<List> hr = exchange(HttpMethod.GET, "/employees?department=HR", token, null, List.class);

        assertThat(all.getBody()).hasSize(2);
        assertThat((List<Map<String, Object>>) hr.getBody())
                .extracting(employee -> employee.get("name"))
                .containsExactly("Bob");
    }

    @Test
    void getEmployeeReturns404ForAnUnknownOrMalformedId() {
        String token = registerAndLogin("ada@example.com");

        assertThat(exchange(HttpMethod.GET, "/employees/" + new ObjectId().toHexString(), token, null, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange(HttpMethod.GET, "/employees/not-an-id", token, null, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void onlyTheCreatorCanDeleteAnEmployee() {
        String creatorToken = registerAndLogin("ada@example.com");
        String otherToken = registerAndLogin("grace@example.com");
        String employeeId = createEmployee(creatorToken, ADA);

        ResponseEntity<Map> forbidden =
                exchange(HttpMethod.DELETE, "/employees/" + employeeId, otherToken, null, Map.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forbidden.getBody())
                .containsEntry("error", "Only creator of an employee can delete that employee.");
        assertThat(employeeRepository.existsById(employeeId)).isTrue();

        ResponseEntity<Void> deleted =
                exchange(HttpMethod.DELETE, "/employees/" + employeeId, creatorToken, null, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(employeeRepository.existsById(employeeId)).isFalse();
    }
}
