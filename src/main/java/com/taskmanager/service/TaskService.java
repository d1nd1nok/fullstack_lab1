package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.BusinessLogicException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена с ID: " + id));
        return convertToDTO(task);
    }

    public TaskDTO createTask(Task task) {
        // Auto-assign to an available user with less than 3 active tasks
        List<User> availableUsers = userRepository.findAvailableUsers();
        if (availableUsers.isEmpty()) {
            throw new BusinessLogicException("Нет доступных пользователей для назначения задачи");
        }

        User assignee = null;
        for (User u : availableUsers) {
            long activeCount = taskRepository.countActiveTasksByUserId(u.getId());
            if (activeCount < 3) {
                assignee = u;
                break;
            }
        }

        if (assignee == null) {
            throw new BusinessLogicException("Все доступные пользователи имеют 3 активные задачи");
        }

        task.setAssignedUser(assignee);
        task.setDone(false);
        task.setCreationTimestamp(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        // After assignment, if this is the 3rd active task, mark user BUSY
        long activeAfter = taskRepository.countActiveTasksByUserId(assignee.getId());
        if (activeAfter >= 3) {
            assignee.setAvailabilityStatus(AvailabilityStatus.BUSY);
            userRepository.save(assignee);
        }

        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена с ID: " + id));

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public TaskDTO updateTaskDone(Long id, boolean done) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена с ID: " + id));

        task.setDone(done);
        Task updatedTask = taskRepository.save(task);

        // Recalculate availability for the assigned user
        if (updatedTask.getAssignedUser() != null) {
            User assigned = updatedTask.getAssignedUser();
            long active = taskRepository.countActiveTasksByUserId(assigned.getId());
            assigned.setAvailabilityStatus(active >= 3 ? AvailabilityStatus.BUSY : AvailabilityStatus.AVAILABLE);
            userRepository.save(assigned);
        }

        return convertToDTO(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена с ID: " + id));
        
        User assignedUser = task.getAssignedUser();
        taskRepository.delete(task);
        
        // Recalculate availability for the assigned user after deletion
        if (assignedUser != null) {
            long active = taskRepository.countActiveTasksByUserId(assignedUser.getId());
            assignedUser.setAvailabilityStatus(active >= 3 ? AvailabilityStatus.BUSY : AvailabilityStatus.AVAILABLE);
            userRepository.save(assignedUser);
        }
    }

    public List<TaskDTO> getTasksByPriority(PriorityLevel priorityLevel) {
        return taskRepository.findByPriorityLevel(priorityLevel).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByAssignee(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));
        return taskRepository.findByAssignedUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public TaskDTO assignTaskToAvailableUser(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена с ID: " + taskId));

        User previousAssignee = task.getAssignedUser();
        
        List<User> availableUsers = userRepository.findAvailableUsers();
        if (availableUsers.isEmpty()) {
            throw new BusinessLogicException("Нет доступных пользователей для назначения задачи");
        }

        // Choose first available user with < 3 active tasks
        User chosen = null;
        for (User u : availableUsers) {
            long active = taskRepository.countActiveTasksByUserId(u.getId());
            if (active < 3) {
                chosen = u;
                break;
            }
        }

        if (chosen == null) {
            throw new BusinessLogicException("Все доступные пользователи имеют 3 активные задачи");
        }

        task.setAssignedUser(chosen);
        Task updatedTask = taskRepository.save(task);

        // Recalculate availability for the new assignee
        long activeAfter = taskRepository.countActiveTasksByUserId(chosen.getId());
        chosen.setAvailabilityStatus(activeAfter >= 3 ? AvailabilityStatus.BUSY : AvailabilityStatus.AVAILABLE);
        userRepository.save(chosen);

        // Recalculate availability for the previous assignee (if any)
        if (previousAssignee != null && !previousAssignee.getId().equals(chosen.getId())) {
            long previousActive = taskRepository.countActiveTasksByUserId(previousAssignee.getId());
            previousAssignee.setAvailabilityStatus(previousActive >= 3 ? AvailabilityStatus.BUSY : AvailabilityStatus.AVAILABLE);
            userRepository.save(previousAssignee);
        }

        return convertToDTO(updatedTask);
    }

    private TaskDTO convertToDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriorityLevel(),
                task.getCreationTimestamp(),
                task.getAssignedUser() != null ? task.getAssignedUser().getId() : null,
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : null,
                task.isDone()
        );
    }
}