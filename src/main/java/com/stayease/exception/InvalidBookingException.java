package com.stayease.exception;

/**
 * Thrown when booking input fails business validation
 * (e.g. check-out before check-in, guest count over room capacity).
 */
public class InvalidBookingException extends Exception {

    public InvalidBookingException(String message) {
        super(message);
    }
}
