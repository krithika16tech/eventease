package com.eventease.exception;

public class OrganizerNotFoundException extends RuntimeException {
    public OrganizerNotFoundException(String message) {
        super(message);
    }
}
