package com.example.employeeapi.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.example.employeeapi.authentication.AuthUser;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    private static final String CONNECTED_USER_ID = new ObjectId().toHexString();

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        var connectedUser = new AuthUser(CONNECTED_USER_ID, "ada@example.com", "Ada", "Lovelace");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(connectedUser, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static User user(String id) {
        User user = new User("ada@example.com", "hashed", "Ada", "Lovelace");
        user.setId(id);
        return user;
    }

    @Test
    void getCurrentUserReturnsTheConnectedUserWithoutThePassword() throws Exception {
        when(userService.getUser(CONNECTED_USER_ID)).thenReturn(Optional.of(user(CONNECTED_USER_ID)));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(CONNECTED_USER_ID))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getCurrentUserReturns404WhenTheAccountNoLongerExists() throws Exception {
        when(userService.getUser(CONNECTED_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/me")).andExpect(status().isNotFound());
    }

    @Test
    void getUserByIdReturnsTheUserWithoutThePassword() throws Exception {
        String id = new ObjectId().toHexString();
        when(userService.getUser(id)).thenReturn(Optional.of(user(id)));

        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(id))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserByIdReturns404WhenIdIsNotAnObjectId() throws Exception {
        mockMvc.perform(get("/users/not-an-id")).andExpect(status().isNotFound());

        verify(userService, never()).getUser(any());
    }

    @Test
    void getUserByIdReturns404WhenTheUserDoesNotExist() throws Exception {
        String id = new ObjectId().toHexString();
        when(userService.getUser(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/" + id)).andExpect(status().isNotFound());
    }
}
