package com.example.employeeapi.authentication;

import java.util.Map;
import java.util.Optional;

import com.example.employeeapi.core.validation.RequestValidator;
import com.example.employeeapi.users.CreatedUser;
import com.example.employeeapi.users.User;
import com.example.employeeapi.users.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Routes d'inscription et de connexion. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Map<String, String> INVALID_CREDENTIALS = Map.of("error", "Invalid credentials");

    private final UserService userService;
    private final RequestValidator requestValidator;

    public AuthController(UserService userService, RequestValidator requestValidator) {
        this.userService = userService;
        this.requestValidator = requestValidator;
    }

    @PostMapping("/register")
    public ResponseEntity<CreatedUser> register(@RequestBody(required = false) RegisterRequest body) {
        RegisterRequest request = body != null ? body : RegisterRequest.empty();
        requestValidator.validate(request);

        CreatedUser createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody(required = false) LoginRequest body) {
        LoginRequest request = body != null ? body : LoginRequest.empty();
        requestValidator.validate(request);

        Optional<User> user = userService.getUserByEmail(request.email());
        if (user.isEmpty() || !userService.checkPassword(request.password(), user.get().getPassword())) {
            // Même réponse dans les deux cas, pour ne pas révéler si l'email existe
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS);
        }

        String token = userService.generateAuthToken(user.get());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
