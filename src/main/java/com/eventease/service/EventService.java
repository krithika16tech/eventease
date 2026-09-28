package com.eventease.service;

import com.eventease.dto.EventCreateRequest;
import com.eventease.dto.EventResponse;
import com.eventease.dto.StudentResponse;
import com.eventease.entity.Event;
import com.eventease.entity.Organizer;
import com.eventease.entity.Registration;
import com.eventease.entity.RegistrationStatus;
import com.eventease.entity.Student;
import com.eventease.exception.EventNotFoundException;
import com.eventease.exception.OrganizerNotFoundException;
import com.eventease.repository.EventRepository;
import com.eventease.repository.OrganizerRepository;
import com.eventease.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final RegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository,
                        OrganizerRepository organizerRepository,
                        RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
        this.registrationRepository = registrationRepository;
    }

    public EventResponse createEvent(EventCreateRequest request) {
        Organizer organizer = organizerRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer not found with ID: " + request.getOrganizerId()));

        Event event = new Event(
                request.getTitle(),
                request.getEventDate(),
                request.getVenue(),
                request.getMaxSeats(),
                organizer
        );

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getRegisteredParticipants(Long eventId) {
        // Feature 4: Check whether event exists
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }

        // Return active registered students for the event
        List<Registration> activeRegistrations = registrationRepository.findByEventIdAndStatus(eventId, RegistrationStatus.ACTIVE);
        return activeRegistrations.stream()
                .map(Registration::getStudent)
                .map(student -> new StudentResponse(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getRollNumber()
                ))
                .collect(Collectors.toList());
    }

    public EventResponse mapToResponse(Event event) {
        long activeCount = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.ACTIVE);
        int availableSeats = Math.max(0, event.getMaxSeats() - (int) activeCount);

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getVenue(),
                event.getMaxSeats(),
                activeCount,
                availableSeats,
                event.getOrganizer().getId(),
                event.getOrganizer().getName()
        );
    }
}
