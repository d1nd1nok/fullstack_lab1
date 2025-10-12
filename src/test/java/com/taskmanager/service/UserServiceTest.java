package com.taskmanager.service;

import com.taskmanager.dto.CreateUserRequest;
import com.taskmanager.entity.User;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setHashedPassword("encodedPassword");
        user.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("newuser");
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setPassword("Password123!");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // When
        var result = userService.getAllUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        var result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        var result = userService.createUser(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        createUserRequest.setEmail("updated@example.com");
        createUserRequest.setPassword("NewPassword123!");

        // When
        var result = userService.updateUser(1L, createUserRequest);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("updated@example.com");
        verify(passwordEncoder, times(1)).encode("NewPassword123!");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        createUserRequest.setEmail("existing@example.com");

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, createUserRequest));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithEmptyPassword_ShouldNotEncodePassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        createUserRequest.setEmail("updated@example.com");
        createUserRequest.setPassword(""); // Empty password

        // When
        userService.updateUser(1L, createUserRequest);

        // Then
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getAvailableUsers_ShouldReturnAvailableUsers() {
        // Given
        List<User> availableUsers = Arrays.asList(user);
        when(userRepository.findAvailableUsers()).thenReturn(availableUsers);

        // When
        var result = userService.getAvailableUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals(AvailabilityStatus.AVAILABLE, result.get(0).getAvailabilityStatus());
        verify(userRepository, times(1)).findAvailableUsers();
    }

    @Test
    void getAvailableUsers_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAvailableUsers()).thenReturn(Arrays.asList());

        // When
        var result = userService.getAvailableUsers();

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAvailableUsers();
    }
}
