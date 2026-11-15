package com.example.employeeapi.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.employeeapi.authentication.JwtService;
import com.example.employeeapi.authentication.RegisterRequest;
import com.example.employeeapi.core.error.DuplicateFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final RegisterRequest REGISTRATION =
            new RegisterRequest("ada@example.com", "Passw0rd!", "Ada", "Lovelace");

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserStoresTheHashedPasswordAndReturnsTheUserWithoutIt() {
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("66b0c0ffee0000000000abcd");
            return user;
        });

        CreatedUser created = userService.registerUser(REGISTRATION);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
        assertThat(created)
                .isEqualTo(new CreatedUser("ada@example.com", "Ada", "Lovelace", "66b0c0ffee0000000000abcd"));
    }

    @Test
    void registerUserReportsAnAlreadyUsedEmail() {
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenThrow(new DuplicateKeyException("E11000"));

        assertThatThrownBy(() -> userService.registerUser(REGISTRATION))
                .isInstanceOfSatisfying(DuplicateFieldException.class, e -> {
                    assertThat(e.getField()).isEqualTo("email");
                    assertThat(e.getValue()).isEqualTo("ada@example.com");
                });
    }

    @Test
    void checkPasswordDelegatesToThePasswordEncoder() {
        when(passwordEncoder.matches("Passw0rd!", "hashed")).thenReturn(true);

        assertThat(userService.checkPassword("Passw0rd!", "hashed")).isTrue();
    }

    @Test
    void generateAuthTokenDelegatesToTheJwtService() {
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        when(jwtService.generateToken(user)).thenReturn("a.jwt.token");

        assertThat(userService.generateAuthToken(user)).isEqualTo("a.jwt.token");
    }
}
