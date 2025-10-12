package com.taskmanager.dto;

import com.taskmanager.enums.PriorityLevel;

import java.time.LocalDateTime;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private PriorityLevel priorityLevel;
    private LocalDateTime creationTimestamp;
    private Long assignedUserId;
    private String assignedUserName;
    private boolean done;

    public TaskDTO() {
    }

    public TaskDTO(Long id, String title, String description, PriorityLevel priorityLevel, LocalDateTime creationTimestamp, Long assignedUserId, String assignedUserName, boolean done) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.creationTimestamp = creationTimestamp;
        this.assignedUserId = assignedUserId;
        this.assignedUserName = assignedUserName;
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public String getAssignedUserName() {
        return assignedUserName;
    }

    public void setAssignedUserName(String assignedUserName) {
        this.assignedUserName = assignedUserName;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
