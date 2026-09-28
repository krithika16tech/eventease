package com.eventease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StudentCreateRequest {

    @NotBlank(message = "Student name cannot be empty")
    private String name;

    @NotBlank(message = "Student email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Student roll number cannot be empty")
    private String rollNumber;

    public StudentCreateRequest() {
    }

    public StudentCreateRequest(String name, String email, String rollNumber) {
        this.name = name;
        this.email = email;
        this.rollNumber = rollNumber;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
