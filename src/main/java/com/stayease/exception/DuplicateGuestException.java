package com.stayease.exception;

/** Thrown when attempting to register a guest whose NIC/passport already exists. */
public class DuplicateGuestException extends Exception {

    public DuplicateGuestException(String message) {
        super(message);
    }
}
