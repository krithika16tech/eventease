package com.eventease.controller;

import com.eventease.dto.OrganizerCreateRequest;
import com.eventease.dto.OrganizerResponse;
import com.eventease.service.OrganizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizers")
@Tag(name = "Organizers", description = "Endpoints for managing event organizers")
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping
    @Operation(summary = "Create a new organizer", description = "Create an organizer with name, email, and department.")
    public ResponseEntity<OrganizerResponse> createOrganizer(@Valid @RequestBody OrganizerCreateRequest request) {
        OrganizerResponse response = organizerService.createOrganizer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all organizers", description = "Retrieve list of all organizers.")
    public ResponseEntity<List<OrganizerResponse>> getAllOrganizers() {
        return ResponseEntity.ok(organizerService.getAllOrganizers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organizer by ID", description = "Retrieve an organizer by ID.")
    public ResponseEntity<OrganizerResponse> getOrganizerById(@PathVariable Long id) {
        return ResponseEntity.ok(organizerService.getOrganizerById(id));
    }
}
