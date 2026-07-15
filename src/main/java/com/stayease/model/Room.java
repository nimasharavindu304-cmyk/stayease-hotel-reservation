package com.stayease.model;

import java.math.BigDecimal;

/** A physical, bookable hotel room. */
public class Room {

    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal ratePerNight;
    private int capacity;
    private RoomStatus status;
    private String description;

    public Room() {
    }

    public Room(int roomId, String roomNumber, RoomType roomType, BigDecimal ratePerNight,
                int capacity, RoomStatus status, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.ratePerNight = ratePerNight;
        this.capacity = capacity;
        this.status = status;
        this.description = description;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(BigDecimal ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " - " + roomType;
    }
}
