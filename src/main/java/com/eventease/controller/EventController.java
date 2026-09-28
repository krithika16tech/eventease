package com.eventease.controller;

import com.eventease.dto.EventCreateRequest;
import com.eventease.dto.EventResponse;
import com.eventease.dto.RegistrationResponse;
import com.eventease.dto.StudentResponse;
import com.eventease.service.EventService;
import com.eventease.service.RegistrationService;
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
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints for managing college events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;

    public EventController(EventService eventService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @PostMapping
    @Operation(summary = "Create a new event", description = "Feature 1: An organizer creates an event with title, date, venue, and maximum seats.")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieve all events with capacity details.")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve single event with capacity and registration count.")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "View registered participants", description = "Feature 4: Organizer views registered participants for a particular event.")
    public ResponseEntity<List<StudentResponse>> getRegisteredParticipants(@PathVariable Long id) {
        List<StudentResponse> participants = eventService.getRegisteredParticipants(id);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/{id}/registrations")
    @Operation(summary = "Get all registrations for an event", description = "Retrieve registrations including status for an event.")
    public ResponseEntity<List<RegistrationResponse>> getEventRegistrations(@PathVariable Long id) {
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByEvent(id);
        return ResponseEntity.ok(registrations);
    }
}
