package com.eventease.service;

import com.eventease.dto.RegistrationRequest;
import com.eventease.dto.RegistrationResponse;
import com.eventease.entity.Event;
import com.eventease.entity.Registration;
import com.eventease.entity.RegistrationStatus;
import com.eventease.entity.Student;
import com.eventease.exception.CancellationNotAllowedException;
import com.eventease.exception.DuplicateRegistrationException;
import com.eventease.exception.EventFullException;
import com.eventease.exception.EventNotFoundException;
import com.eventease.exception.InvalidOperationException;
import com.eventease.exception.RegistrationNotFoundException;
import com.eventease.exception.StudentNotFoundException;
import com.eventease.repository.EventRepository;
import com.eventease.repository.RegistrationRepository;
import com.eventease.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               StudentRepository studentRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Feature 2 & 3: Student registers for an event.
     * Enforces event existence, student existence, duplicate registration check,
     * and capacity limit.
     */
    public RegistrationResponse registerStudent(RegistrationRequest request) {
        // Step 1: Check whether the event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + request.getEventId()));

        // Step 2: Check whether the student exists
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + request.getStudentId()));

        // Step 3: Check whether the student has already registered for the same event
        boolean alreadyRegistered = registrationRepository.existsByStudentIdAndEventIdAndStatus(
                student.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE
        );
        if (alreadyRegistered) {
            throw new DuplicateRegistrationException("Student with ID " + student.getId() +
                    " ('" + student.getName() + "') is already registered for event ID " +
                    event.getId() + " ('" + event.getTitle() + "').");
        }

        // Step 4 & 5: Check whether seats are still available and reject if capacity reached
        long activeCount = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.ACTIVE);
        if (activeCount >= event.getMaxSeats()) {
            throw new EventFullException("Event '" + event.getTitle() + "' is full. Maximum capacity of " +
                    event.getMaxSeats() + " seats has been reached.");
        }

        // Step 6: Create registration only when a seat is available
        Registration registration = new Registration(student, event);
        Registration saved = registrationRepository.save(registration);
        return mapToResponse(saved);
    }

    /**
     * Feature 5: Student cancels their registration before the event date.
     * Enforces ownership, event date check, frees seat.
     */
    public RegistrationResponse cancelRegistration(Long registrationId, Long studentId) {
        // Step 1: Check whether registration exists
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with ID: " + registrationId));

        // Check if registration is already cancelled
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new InvalidOperationException("Registration with ID " + registrationId + " is already cancelled.");
        }

        // Step 2: Rule - A student can cancel only their own registration
        if (studentId != null && !registration.getStudent().getId().equals(studentId)) {
            throw new InvalidOperationException("Student with ID " + studentId +
                    " is not authorized to cancel this registration. A student can only cancel their own registration.");
        }

        // Step 3: Rule - Cancellation is allowed only before the event date (cannot cancel on or after event date)
        LocalDate today = LocalDate.now();
        LocalDate eventDate = registration.getEvent().getEventDate();
        if (!today.isBefore(eventDate)) {
            throw new CancellationNotAllowedException("Cannot cancel registration on or after the event date. Event date: " +
                    eventDate + ", Current date: " + today + ".");
        }

        // Step 4: Rule - Cancelled registration must not remain counted as an active registration
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancellationDate(LocalDateTime.now());

        Registration updated = registrationRepository.save(registration);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with ID: " + registrationId));
        return mapToResponse(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student not found with ID: " + studentId);
        }
        return registrationRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RegistrationResponse mapToResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getStudent().getId(),
                registration.getStudent().getName(),
                registration.getStudent().getEmail(),
                registration.getStudent().getRollNumber(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getEventDate(),
                registration.getStatus(),
                registration.getRegistrationDate(),
                registration.getCancellationDate()
        );
    }
}
