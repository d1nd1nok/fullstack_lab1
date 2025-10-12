package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.exception.BusinessLogicException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;
    private List<User> availableUsers;

    @BeforeEach
    void setUp() {
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

        availableUsers = Arrays.asList(user);
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(tasks);

        // When
        var result = taskService.getAllTasks();

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Task", result.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        var result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_WhenTaskNotExists_ShouldThrowException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void createTask_WithAvailableUser_ShouldCreateAndAssignTask() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setPriorityLevel(PriorityLevel.MEDIUM);

        when(userRepository.findAvailableUsers()).thenReturn(availableUsers);
        when(taskRepository.countActiveTasksByUserId(1L)).thenReturn(1L);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        var result = taskService.createTask(newTask);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findAvailableUsers();
        verify(taskRepository, times(1)).countActiveTasksByUserId(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithNoAvailableUsers_ShouldThrowException() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");

        when(userRepository.findAvailableUsers()).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(BusinessLogicException.class, () -> taskService.createTask(newTask));
        verify(userRepository, times(1)).findAvailableUsers();
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WhenUserHasThreeActiveTasks_ShouldMarkUserAsBusy() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");

        when(userRepository.findAvailableUsers()).thenReturn(availableUsers);
        when(taskRepository.countActiveTasksByUserId(1L)).thenReturn(2L, 3L); // Before and after
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        taskService.createTask(newTask);

        // Then
        assertEquals(AvailabilityStatus.BUSY, user.getAvailabilityStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateTask_WithValidData_ShouldUpdateTask() {
        // Given
        Task updateData = new Task();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        var result = taskService.updateTask(1L, updateData);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", task.getTitle());
        assertEquals("Updated Description", task.getDescription());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskDone_WithValidData_ShouldUpdateDoneAndRecalculateStatus() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskRepository.countActiveTasksByUserId(1L)).thenReturn(2L);

        // When
        var result = taskService.updateTaskDone(1L, true);

        // Then
        assertNotNull(result);
        assertTrue(task.isDone());
        assertEquals(AvailabilityStatus.AVAILABLE, user.getAvailabilityStatus());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteTask_ShouldDeleteTaskAndRecalculateUserStatus() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.countActiveTasksByUserId(1L)).thenReturn(2L);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(task);
        verify(taskRepository, times(1)).countActiveTasksByUserId(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void assignTaskToAvailableUser_ShouldAssignTaskAndUpdateStatuses() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        List<User> availableUsersList = Arrays.asList(newUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findAvailableUsers()).thenReturn(availableUsersList);
        when(taskRepository.countActiveTasksByUserId(2L)).thenReturn(1L, 2L);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        var result = taskService.assignTaskToAvailableUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(newUser, task.getAssignedUser());
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findAvailableUsers();
        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void getTasksByPriority_ShouldReturnTasksWithSpecificPriority() {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByPriorityLevel(PriorityLevel.HIGH)).thenReturn(tasks);

        // When
        var result = taskService.getTasksByPriority(PriorityLevel.HIGH);

        // Then
        assertEquals(1, result.size());
        assertEquals(PriorityLevel.HIGH, result.get(0).getPriorityLevel());
        verify(taskRepository, times(1)).findByPriorityLevel(PriorityLevel.HIGH);
    }

    @Test
    void getTasksByAssignee_ShouldReturnTasksForSpecificUser() {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findByAssignedUser(user)).thenReturn(tasks);

        // When
        var result = taskService.getTasksByAssignee(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAssignedUserId());
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).findByAssignedUser(user);
    }

    @Test
    void getTasksByAssignee_WhenUserNotExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByAssignee(1L));
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).findByAssignedUser(any(User.class));
    }
}

