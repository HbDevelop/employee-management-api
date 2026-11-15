package com.example.employeeapi.users;

import java.util.Optional;

import com.example.employeeapi.authentication.JwtService;
import com.example.employeeapi.authentication.RegisterRequest;
import com.example.employeeapi.core.error.DuplicateFieldException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Règles métier des comptes : inscription, mot de passe, jeton. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public PublicUser registerUser(RegisterRequest request) {
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName());
        try {
            User saved = userRepository.save(user);
            return PublicUser.from(saved);
        } catch (DuplicateKeyException e) {
            // L'email est déjà pris
            throw new DuplicateFieldException("email", request.email(), e);
        }
    }

    public Optional<User> getUser(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean checkPassword(String plainText, String hashedPassword) {
        return passwordEncoder.matches(plainText, hashedPassword);
    }

    public String generateAuthToken(User user) {
        return jwtService.generateToken(user);
    }
}
