package com.taskmanager.dto;

import com.taskmanager.enums.AvailabilityStatus;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private AvailabilityStatus availabilityStatus;

    public UserDTO() {}

    public UserDTO(AvailabilityStatus availabilityStatus, String email, String username, Long id) {
        this.availabilityStatus = availabilityStatus;
        this.email = email;
        this.username = username;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
