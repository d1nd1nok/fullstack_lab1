package com.taskmanager.dto;

import com.taskmanager.enums.AvailabilityStatus;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private AvailabilityStatus availabilityStatus;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, AvailabilityStatus availabilityStatus) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.availabilityStatus = availabilityStatus;
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
