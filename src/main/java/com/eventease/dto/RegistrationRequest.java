package com.eventease.dto;

import jakarta.validation.constraints.NotNull;

public class RegistrationRequest {

    @NotNull(message = "Student ID cannot be null")
    private Long studentId;

    @NotNull(message = "Event ID cannot be null")
    private Long eventId;

    public RegistrationRequest() {
    }

    public RegistrationRequest(Long studentId, Long eventId) {
        this.studentId = studentId;
        this.eventId = eventId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
