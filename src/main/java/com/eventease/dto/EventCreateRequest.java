package com.eventease.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class EventCreateRequest {

    @NotBlank(message = "Event title cannot be empty")
    private String title;

    @NotNull(message = "Event date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @NotBlank(message = "Venue cannot be empty")
    private String venue;

    @NotNull(message = "Maximum seats cannot be null")
    @Positive(message = "Maximum seats must be positive")
    private Integer maxSeats;

    @NotNull(message = "Organizer ID cannot be null")
    private Long organizerId;

    public EventCreateRequest() {
    }

    public EventCreateRequest(String title, LocalDate eventDate, String venue, Integer maxSeats, Long organizerId) {
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.maxSeats = maxSeats;
        this.organizerId = organizerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Integer getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
}
