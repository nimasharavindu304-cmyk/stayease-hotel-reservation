package com.stayease.model;

import java.time.LocalDateTime;

/** A hotel guest. */
public class Guest {

    private int guestId;
    private String firstName;
    private String lastName;
    private String nicPassport;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;

    public Guest() {
    }

    public Guest(int guestId, String firstName, String lastName, String nicPassport,
                 String phone, String email, String address) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nicPassport = nicPassport;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNicPassport() {
        return nicPassport;
    }

    public void setNicPassport(String nicPassport) {
        this.nicPassport = nicPassport;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + nicPassport + ")";
    }
}
