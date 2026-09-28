package com.eventease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OrganizerCreateRequest {

    @NotBlank(message = "Organizer name cannot be empty")
    private String name;

    @NotBlank(message = "Organizer email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    private String department;

    public OrganizerCreateRequest() {
    }

    public OrganizerCreateRequest(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
