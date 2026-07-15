package com.stayease.exception;

/**
 * Thrown when a booking is requested for a room that is already reserved
 * for an overlapping date range, or is under maintenance.
 * User-defined checked exception required by the coursework brief.
 */
public class RoomNotAvailableException extends Exception {

    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
