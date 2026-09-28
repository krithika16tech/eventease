package com.eventease.controller;

import com.eventease.dto.RegistrationRequest;
import com.eventease.dto.RegistrationResponse;
import com.eventease.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@Tag(name = "Registrations", description = "Endpoints for event registrations and cancellations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @Operation(
            summary = "Register a student for an event",
            description = "Features 2 & 3: Student registers for an event. Enforces existence, duplicate check, and automatic capacity closure."
    )
    public ResponseEntity<RegistrationResponse> registerStudent(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.registerStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a registration (PUT)",
            description = "Feature 5: Student cancels registration before the event date, freeing a seat."
    )
    public ResponseEntity<RegistrationResponse> cancelRegistrationPut(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId) {
        RegistrationResponse response = registrationService.cancelRegistration(id, studentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel a registration (DELETE)",
            description = "Feature 5: Student cancels registration before the event date, freeing a seat."
    )
    public ResponseEntity<RegistrationResponse> cancelRegistrationDelete(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId) {
        RegistrationResponse response = registrationService.cancelRegistration(id, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get registration by ID", description = "Retrieve registration details.")
    public ResponseEntity<RegistrationResponse> getRegistrationById(@PathVariable Long id) {
        RegistrationResponse response = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get registrations by student ID", description = "Retrieve all registrations for a student.")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByStudent(@PathVariable Long studentId) {
        List<RegistrationResponse> list = registrationService.getRegistrationsByStudent(studentId);
        return ResponseEntity.ok(list);
    }
}
