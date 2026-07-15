package com.stayease.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * The "subject" half of the Observer pattern. {@link com.stayease.service.BookingService}
 * calls {@link #notifyObservers()} after every successful booking/payment/check-out so
 * that any registered {@link DashboardObserver} (typically the main dashboard frame)
 * can refresh its statistics without polling the database.
 */
public class BookingSubject {

    private final List<DashboardObserver> observers = new ArrayList<>();

    public void addObserver(DashboardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(DashboardObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (DashboardObserver o : observers) {
            o.onBookingChanged();
        }
    }
}
