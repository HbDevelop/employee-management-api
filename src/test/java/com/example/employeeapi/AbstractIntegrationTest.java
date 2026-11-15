package com.example.employeeapi;

import java.util.Map;

import com.example.employeeapi.employees.EmployeeRepository;
import com.example.employeeapi.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

/** Base des tests d'intégration : démarre l'application complète avec un vrai MongoDB temporaire. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "app.security.jwt-secret=" + AbstractIntegrationTest.JWT_SECRET,
        "app.security.jwt-ttl=1d",
        // Protection des mots de passe au minimum, pour des tests rapides
        "app.security.bcrypt-strength=4"
})
public abstract class AbstractIntegrationTest {

    protected static final String JWT_SECRET = "integration-test-secret-integration-test-0123456789";
    protected static final String PASSWORD = "Passw0rd!";

    // Un seul MongoDB temporaire, partagé par tous les tests
    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static {
        MONGO.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MONGO.getReplicaSetUrl("employee_api_it"));
    }

    @Autowired
    protected TestRestTemplate rest;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected EmployeeRepository employeeRepository;

    @AfterEach
    void cleanDatabase() {
        employeeRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected static Map<String, Object> registration(String email) {
        return Map.of("email", email, "password", PASSWORD, "firstName", "Ada", "lastName", "Lovelace");
    }

    /** Crée un compte et renvoie son identifiant. */
    protected String register(String email) {
        return (String) rest.postForObject("/auth/register", registration(email), Map.class).get("_id");
    }

    /** Crée un compte, se connecte, et renvoie le jeton. */
    protected String registerAndLogin(String email) {
        register(email);
        Map<?, ?> body = rest.postForObject("/auth/login", Map.of("email", email, "password", PASSWORD), Map.class);
        return (String) body.get("token");
    }

    protected <T> ResponseEntity<T> exchange(HttpMethod method, String path, String token, Object body,
                                             Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange(path, method, new HttpEntity<>(body, headers), responseType);
    }
}
