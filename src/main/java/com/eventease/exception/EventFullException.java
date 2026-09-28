package com.eventease.exception;

public class EventFullException extends RuntimeException {
    public EventFullException(String message) {
        super(message);
    }
}
