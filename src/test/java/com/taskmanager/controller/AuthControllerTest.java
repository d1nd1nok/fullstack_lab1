package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.CreateUserRequest;
import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.entity.User;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtUtil;
import com.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setHashedPassword("encodedPassword");
        user.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        userDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                AvailabilityStatus.AVAILABLE
        );

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("newuser");
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");
    }

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() throws Exception {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDTO);
        when(jwtUtil.generateToken("newuser")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
        verify(jwtUtil, times(1)).generateToken("newuser");
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("Password123!", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        loginRequest.setUsername("invaliduser");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(1)).findByUsername("invaliduser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        loginRequest.setPassword("WrongPassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("WrongPassword", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void register_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void login_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void register_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest invalidRequest = new CreateUserRequest();
        invalidRequest.setUsername(""); // Empty username
        invalidRequest.setEmail("invalid-email"); // Invalid email
        invalidRequest.setPassword("123"); // Too short password

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }
}
