package com.eventease.repository;

import com.eventease.entity.Registration;
import com.eventease.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Checking duplicate registration
    boolean existsByStudentIdAndEventIdAndStatus(Long studentId, Long eventId, RegistrationStatus status);

    // Counting active registrations for an event
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    // Finding registrations for a particular event by status
    List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    // Finding all registrations for a particular event
    List<Registration> findByEventId(Long eventId);

    // Finding a student's active registration for an event
    Optional<Registration> findByStudentIdAndEventIdAndStatus(Long studentId, Long eventId, RegistrationStatus status);

    // Finding all registrations of a student
    List<Registration> findByStudentId(Long studentId);
}
