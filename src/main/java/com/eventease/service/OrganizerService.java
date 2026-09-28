package com.eventease.service;

import com.eventease.dto.OrganizerCreateRequest;
import com.eventease.dto.OrganizerResponse;
import com.eventease.entity.Organizer;
import com.eventease.exception.OrganizerNotFoundException;
import com.eventease.repository.OrganizerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    public OrganizerService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    public OrganizerResponse createOrganizer(OrganizerCreateRequest request) {
        if (organizerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Organizer with email '" + request.getEmail() + "' already exists.");
        }

        Organizer organizer = new Organizer(request.getName(), request.getEmail(), request.getDepartment());
        Organizer saved = organizerRepository.save(organizer);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizerResponse getOrganizerById(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer not found with ID: " + id));
        return mapToResponse(organizer);
    }

    @Transactional(readOnly = true)
    public List<OrganizerResponse> getAllOrganizers() {
        return organizerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizerResponse mapToResponse(Organizer organizer) {
        return new OrganizerResponse(
                organizer.getId(),
                organizer.getName(),
                organizer.getEmail(),
                organizer.getDepartment()
        );
    }
}
