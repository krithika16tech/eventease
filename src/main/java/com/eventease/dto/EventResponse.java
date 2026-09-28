package com.eventease.dto;

import java.time.LocalDate;

public class EventResponse {

    private Long id;
    private String title;
    private LocalDate eventDate;
    private String venue;
    private Integer maxSeats;
    private long activeRegistrationsCount;
    private int availableSeats;
    private Long organizerId;
    private String organizerName;

    public EventResponse() {
    }

    public EventResponse(Long id, String title, LocalDate eventDate, String venue, Integer maxSeats,
                         long activeRegistrationsCount, int availableSeats, Long organizerId, String organizerName) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.maxSeats = maxSeats;
        this.activeRegistrationsCount = activeRegistrationsCount;
        this.availableSeats = availableSeats;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public long getActiveRegistrationsCount() {
        return activeRegistrationsCount;
    }

    public void setActiveRegistrationsCount(long activeRegistrationsCount) {
        this.activeRegistrationsCount = activeRegistrationsCount;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }
}
