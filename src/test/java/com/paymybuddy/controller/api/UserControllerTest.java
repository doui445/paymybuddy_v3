package com.paymybuddy.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.model.User;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper; // For serialize/deserialize JSON

    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        user = User.builder()
                .id(1)
                .username("john")
                .password("johnjohn")
                .email("john@gmail.com")
                .build();
    }

    @Test
    @DisplayName("Create User - Success")
    void givenUser_whenCreateUser_thenReturnCreatedUser() throws Exception {
        User savedUser = User.builder()
                .id(2)
                .username("jane")
                .password("jane")
                .email("jane@gmail.com")
                .build();

        when(userService.saveUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("jane"))
                .andExpect(jsonPath("$.email").value("jane@gmail.com"));
    }

    @Test
    @DisplayName("Get All Users - Success")
    void givenUsers_whenGetUsers_thenReturnUserList() throws Exception {
        given(userService.getUsers()).willReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    @DisplayName("Get User By Id - Success")
    void givenUserId_whenGetUserById_thenReturnUser() throws Exception {
        given(userService.getUserById(user.getId())).willReturn(Optional.of(user)); // User exists

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    @DisplayName("Get User By Id - Not Found")
    void givenNonExistingUserId_whenGetUserById_thenReturnNotFound() throws Exception {
        given(userService.getUserById(any(Integer.class))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update User - Success")
    void givenUser_whenUpdateUser_thenReturnUpdatedUser() throws Exception {
        User user1 = user;
        user1.setEmail("john@icloud.com");

        User userUpdates = User.builder()
                .email("john@icloud.com")
                .build();

        given(userService.getUserById(1)).willReturn(Optional.of(user));
        given(userService.saveUser(any(User.class))).willReturn(user1);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@icloud.com"));
    }

    @Test
    @DisplayName("Update User - Not Found")
    void givenNonExistingUser_whenUpdateUser_thenReturnNotFound() throws Exception {
        User userUpdates = User.builder()
                .email("john@icloud.com")
                .build();

        given(userService.getUserById(1)).willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete User - Success")
    void givenUserId_whenDeleteUser_thenReturnOk() throws Exception {
        willDoNothing().given(userService).deleteUserById(user.getId());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(1);
    }
}
