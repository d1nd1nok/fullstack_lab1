package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody Task task) {
        TaskDTO createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody Task task) {
        TaskDTO updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/priority/{priorityLevel}")
    public ResponseEntity<List<TaskDTO>> getTasksByPriority(@PathVariable PriorityLevel priorityLevel) {
        List<TaskDTO> tasks = taskService.getTasksByPriority(priorityLevel);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(@PathVariable Long userId) {
        List<TaskDTO> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/high-priority")
    public ResponseEntity<List<TaskDTO>> getHighPriorityTasks() {
        List<TaskDTO> tasks = taskService.getHighPriorityTasks();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<TaskDTO> assignTaskToAvailableUser(@PathVariable Long taskId) {
        TaskDTO assignedTask = taskService.assignTaskToAvailableUser(taskId);
        return ResponseEntity.ok(assignedTask);
    }
}
