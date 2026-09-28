package com.eventease.dto;

import com.eventease.entity.RegistrationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrationResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentRollNumber;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private RegistrationStatus status;
    private LocalDateTime registrationDate;
    private LocalDateTime cancellationDate;

    public RegistrationResponse() {
    }

    public RegistrationResponse(Long id, Long studentId, String studentName, String studentEmail,
                                String studentRollNumber, Long eventId, String eventTitle,
                                LocalDate eventDate, RegistrationStatus status,
                                LocalDateTime registrationDate, LocalDateTime cancellationDate) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentRollNumber = studentRollNumber;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.status = status;
        this.registrationDate = registrationDate;
        this.cancellationDate = cancellationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }
}
