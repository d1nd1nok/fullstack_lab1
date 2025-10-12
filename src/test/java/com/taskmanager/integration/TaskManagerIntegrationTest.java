package com.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.CreateUserRequest;
import com.taskmanager.dto.LoginRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskManagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("Password123!");

        var registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn();

        var registerContent = registerResponse.getResponse().getContentAsString();
        var registerAuthResponse = objectMapper.readTree(registerContent);
        authToken = registerAuthResponse.get("token").asText();
    }

    @Test
    void fullTaskLifecycle_ShouldWorkCorrectly() throws Exception {
        // 1. Create first task
        Task task1 = new Task();
        task1.setTitle("First Task");
        task1.setDescription("First task description");
        task1.setPriorityLevel(PriorityLevel.HIGH);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First Task"))
                .andExpect(jsonPath("$.assignedUserId").exists())
                .andExpect(jsonPath("$.done").value(false));

        // 2. Create second task
        Task task2 = new Task();
        task2.setTitle("Second Task");
        task2.setDescription("Second task description");
        task2.setPriorityLevel(PriorityLevel.MEDIUM);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Second Task"));

        // 3. Update task
        Task updateTask = new Task();
        updateTask.setTitle("Updated First Task");
        updateTask.setDescription("Updated description");

        mockMvc.perform(put("/api/tasks/1")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated First Task"));

        // 4. Mark task as done
        mockMvc.perform(put("/api/tasks/1/done")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));

        // 5. Get all tasks
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());

        // 6. Get tasks by priority
        mockMvc.perform(get("/api/tasks/priority/HIGH")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 7. Get available users
        mockMvc.perform(get("/api/users/available")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 8. Delete task
        mockMvc.perform(delete("/api/tasks/2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void userRegistrationAndLogin_ShouldWorkCorrectly() throws Exception {
        // 1. Register new user
        CreateUserRequest newUserRequest = new CreateUserRequest();
        newUserRequest.setUsername("newuser");
        newUserRequest.setEmail("newuser@example.com");
        newUserRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token").exists());

        // 2. Login with new user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("newuser");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token").exists());

        // 3. Get user by ID
        mockMvc.perform(get("/api/users/2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void taskAssignment_ShouldWorkCorrectly() throws Exception {
        // Create task
        Task task = new Task();
        task.setTitle("Assignment Test Task");
        task.setDescription("Test task for assignment");
        task.setPriorityLevel(PriorityLevel.URGENT);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        // Assign task to available user
        mockMvc.perform(post("/api/tasks/1/assign")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId").exists());
    }

    @Test
    void errorHandling_ShouldReturnProperErrorCodes() throws Exception {
        // 1. Get non-existent task
        mockMvc.perform(get("/api/tasks/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // 2. Get non-existent user
        mockMvc.perform(get("/api/users/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // 3. Login with wrong credentials
        LoginRequest wrongLoginRequest = new LoginRequest();
        wrongLoginRequest.setUsername("testuser");
        wrongLoginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isBadRequest());

        // 4. Register with existing username
        CreateUserRequest duplicateUserRequest = new CreateUserRequest();
        duplicateUserRequest.setUsername("testuser");
        duplicateUserRequest.setEmail("different@example.com");
        duplicateUserRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUserRequest)))
                .andExpect(status().isBadRequest());
    }
}
