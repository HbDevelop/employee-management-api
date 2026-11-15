package com.example.employeeapi.authentication;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.example.employeeapi.core.error.DuplicateFieldException;
import com.example.employeeapi.core.error.GlobalExceptionHandler;
import com.example.employeeapi.core.validation.RequestValidator;
import com.example.employeeapi.users.CreatedUser;
import com.example.employeeapi.users.User;
import com.example.employeeapi.users.UserService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private static final String VALID_REGISTRATION =
            "{\"email\":\"ada@example.com\",\"password\":\"Passw0rd!\",\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}";
    private static final String VALID_LOGIN = "{\"email\":\"ada@example.com\",\"password\":\"Passw0rd!\"}";

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        var validator = new RequestValidator(Validation.buildDefaultValidatorFactory().getValidator());
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, validator))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsErrorsWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/auth/register"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].field").value("email"))
                .andExpect(jsonPath("$[0].error").value("The user email is required"))
                .andExpect(jsonPath("$[1].error").value("The user password is required"))
                .andExpect(jsonPath("$[2].error").value("The user firstName is required"))
                .andExpect(jsonPath("$[3].error").value("The user lastName is required"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerReturnsErrorsWhenEmailAndPasswordAreInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"anonymousexample.com\",\"password\":\"A68u9\","
                                + "\"firstName\":\"Anonymous\",\"lastName\":\"A.\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].error").value("Invalid email address"))
                .andExpect(jsonPath("$[1].error").value(
                        "The password must contains lowercase uppercase digit and special character and it is 8 characters minimum"));
    }

    @Test
    void registerReturns201WithTheCreatedUser() throws Exception {
        when(userService.registerUser(any()))
                .thenReturn(new CreatedUser("ada@example.com", "Ada", "Lovelace", "66b0c0ffee0000000000abcd"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id").value("66b0c0ffee0000000000abcd"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerReturns400WhenEmailIsAlreadyUsed() throws Exception {
        when(userService.registerUser(any()))
                .thenThrow(new DuplicateFieldException("email", "ada@example.com", null));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("email"))
                .andExpect(jsonPath("$[0].errors[0]").value(
                        "Duplicate constraint violation. The value 'ada@example.com' is already used!!"));
    }

    @Test
    void loginReturns401WhenUserDoesNotExist() throws Exception {
        when(userService.getUserByEmail("ada@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void loginReturns401WhenPasswordIsWrong() throws Exception {
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        when(userService.getUserByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(userService.checkPassword("Passw0rd!", "hashed")).thenReturn(false);

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        verify(userService, never()).generateAuthToken(any());
    }

    @Test
    void loginReturnsATokenWhenCredentialsAreValid() throws Exception {
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        when(userService.getUserByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(userService.checkPassword("Passw0rd!", "hashed")).thenReturn(true);
        when(userService.generateAuthToken(user)).thenReturn("a.jwt.token");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.jwt.token"));
    }
}
