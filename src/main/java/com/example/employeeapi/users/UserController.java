package com.example.employeeapi.users;

import com.example.employeeapi.authentication.AuthUser;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Routes des utilisateurs. Toutes demandent d'être connecté et ne renvoient jamais le mot de passe. */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Renvoie l'utilisateur connecté. */
    @GetMapping("/me")
    public ResponseEntity<PublicUser> getCurrentUser(@AuthenticationPrincipal AuthUser connectedUser) {
        return ResponseEntity.of(userService.getUser(connectedUser.id()).map(PublicUser::from));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PublicUser> getUserById(@PathVariable String userId) {
        if (!ObjectId.isValid(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(userService.getUser(userId).map(PublicUser::from));
    }
}
