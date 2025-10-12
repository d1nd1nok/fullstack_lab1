package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Task task;
    private TaskDTO taskDTO;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Для LocalDateTime

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setPriorityLevel(PriorityLevel.HIGH);
        task.setAssignedUser(user);
        task.setCreationTimestamp(LocalDateTime.now());
        task.setDone(false);

        taskDTO = new TaskDTO(
                1L,
                "Test Task",
                "Test Description",
                PriorityLevel.HIGH,
                LocalDateTime.now(),
                1L,
                "testuser",
                false
        );
    }

    @Test
    void getAllTasks_ShouldReturnListOfTasks() throws Exception {
        // Given
        List<TaskDTO> tasks = Arrays.asList(taskDTO);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].description").value("Test Description"))
                .andExpect(jsonPath("$[0].priorityLevel").value("HIGH"))
                .andExpect(jsonPath("$[0].assignedUserId").value(1L))
                .andExpect(jsonPath("$[0].assignedUserName").value("testuser"))
                .andExpect(jsonPath("$[0].done").value(false));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(taskDTO);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.priorityLevel").value("HIGH"))
                .andExpect(jsonPath("$.assignedUserId").value(1L))
                .andExpect(jsonPath("$.assignedUserName").value("testuser"))
                .andExpect(jsonPath("$.done").value(false));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void createTask_WithValidTask_ShouldReturnCreatedTask() throws Exception {
        // Given
        Task createTask = new Task();
        createTask.setTitle("New Task");
        createTask.setDescription("New Description");
        createTask.setPriorityLevel(PriorityLevel.MEDIUM);

        TaskDTO createdTaskDTO = new TaskDTO(
                2L,
                "New Task",
                "New Description",
                PriorityLevel.MEDIUM,
                LocalDateTime.now(),
                1L,
                "testuser",
                false
        );

        when(taskService.createTask(any(Task.class))).thenReturn(createdTaskDTO);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.priorityLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.assignedUserId").value(1L))
                .andExpect(jsonPath("$.assignedUserName").value("testuser"))
                .andExpect(jsonPath("$.done").value(false));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    void updateTask_WithValidData_ShouldReturnUpdatedTask() throws Exception {
        // Given
        Task updateTask = new Task();
        updateTask.setTitle("Updated Task");
        updateTask.setDescription("Updated Description");

        TaskDTO updatedTaskDTO = new TaskDTO(
                1L,
                "Updated Task",
                "Updated Description",
                PriorityLevel.HIGH,
                LocalDateTime.now(),
                1L,
                "testuser",
                false
        );

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTaskDTO);

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.priorityLevel").value("HIGH"))
                .andExpect(jsonPath("$.assignedUserId").value(1L))
                .andExpect(jsonPath("$.assignedUserName").value("testuser"))
                .andExpect(jsonPath("$.done").value(false));

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    void updateTaskDone_WithValidData_ShouldReturnUpdatedTask() throws Exception {
        // Given
        TaskDTO updatedTaskDTO = new TaskDTO(
                1L,
                "Test Task",
                "Test Description",
                PriorityLevel.HIGH,
                LocalDateTime.now(),
                1L,
                "testuser",
                true
        );

        when(taskService.updateTaskDone(eq(1L), eq(true))).thenReturn(updatedTaskDTO);

        // When & Then
        mockMvc.perform(put("/api/tasks/1/done")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\": true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.done").value(true));

        verify(taskService, times(1)).updateTaskDone(eq(1L), eq(true));
    }

    @Test
    void deleteTask_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void getTasksByPriority_WithValidPriority_ShouldReturnTasks() throws Exception {
        // Given
        List<TaskDTO> tasks = Arrays.asList(taskDTO);
        when(taskService.getTasksByPriority(PriorityLevel.HIGH)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/priority/HIGH"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].priorityLevel").value("HIGH"));

        verify(taskService, times(1)).getTasksByPriority(PriorityLevel.HIGH);
    }

    @Test
    void getTasksByAssignee_WithValidUserId_ShouldReturnTasks() throws Exception {
        // Given
        List<TaskDTO> tasks = Arrays.asList(taskDTO);
        when(taskService.getTasksByAssignee(1L)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/assignee/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].assignedUserId").value(1L));

        verify(taskService, times(1)).getTasksByAssignee(1L);
    }

    @Test
    void assignTaskToAvailableUser_WithValidTaskId_ShouldReturnAssignedTask() throws Exception {
        // Given
        TaskDTO assignedTaskDTO = new TaskDTO(
                1L,
                "Test Task",
                "Test Description",
                PriorityLevel.HIGH,
                LocalDateTime.now(),
                2L,
                "newuser",
                false
        );

        when(taskService.assignTaskToAvailableUser(1L)).thenReturn(assignedTaskDTO);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/assign"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.assignedUserId").value(2L))
                .andExpect(jsonPath("$.assignedUserName").value("newuser"));

        verify(taskService, times(1)).assignTaskToAvailableUser(1L);
    }

    @Test
    void createTask_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void updateTaskDone_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/tasks/1/done")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskDone(anyLong(), anyBoolean());
    }

    @Test
    void getAllTasks_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(taskService.getAllTasks()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getTasksByPriority_WithAllPriorities_ShouldCallServiceCorrectly() throws Exception {
        // Given
        when(taskService.getTasksByPriority(any(PriorityLevel.class))).thenReturn(Arrays.asList(taskDTO));

        // When & Then - Test all priority levels
        mockMvc.perform(get("/api/tasks/priority/LOW"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/priority/MEDIUM"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/priority/HIGH"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/priority/URGENT"))
                .andExpect(status().isOk());

        verify(taskService, times(4)).getTasksByPriority(any(PriorityLevel.class));
    }
}

