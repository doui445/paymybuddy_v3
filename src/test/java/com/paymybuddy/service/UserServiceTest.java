package com.paymybuddy.service;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private AutoCloseable mocks;

    private User user;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("secret")
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("getUserById should return a user when user exists")
    void testGetUserByIdFound() {
        given(userRepository.findById(1)).willReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getUserById should return empty when user does not exist")
    void testGetUserByIdNotFound() {
        given(userRepository.findById(2)).willReturn(Optional.empty());

        Optional<User> result = userService.getUserById(2);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(2);
    }

    @Test
    @DisplayName("getUserByEmail should return a user when email exists")
    void testGetUserByEmailFound() {
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("saveUser should save and return the user")
    void testSaveUser() {
        given(userRepository.save(user)).willReturn(user);

        User savedUser = userService.saveUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("deleteUserById should delete user by id")
    void testDeleteUserById() {
        // There is no return for delete

        userService.deleteUserById(1);

        verify(userRepository, times(1)).deleteById(1);
    }
}
