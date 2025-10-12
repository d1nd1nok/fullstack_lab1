package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.CreateUserRequest;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserDTO userDTO;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

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
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Given
        CreateUserRequest updateRequest = new CreateUserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("NewPassword123!");

        UserDTO updatedUserDTO = new UserDTO(
                1L,
                "updateduser",
                "updated@example.com",
                AvailabilityStatus.AVAILABLE
        );

        when(userService.updateUser(eq(1L), any(CreateUserRequest.class))).thenReturn(updatedUserDTO);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"));

        verify(userService, times(1)).updateUser(eq(1L), any(CreateUserRequest.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getAvailableUsers_ShouldReturnAvailableUsers() throws Exception {
        // Given
        List<UserDTO> availableUsers = Arrays.asList(userDTO);
        when(userService.getAvailableUsers()).thenReturn(availableUsers);

        // When & Then
        mockMvc.perform(get("/api/users/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));

        verify(userService, times(1)).getAvailableUsers();
    }

    @Test
    void updateUser_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(CreateUserRequest.class));
    }

    @Test
    void getAllUsers_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAvailableUsers_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(userService.getAvailableUsers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/users/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAvailableUsers();
    }
}
