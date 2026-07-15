package com.stayease.observer;

/**
 * Observer pattern (Design Pattern #3).
 * <p>
 * Any UI component that needs to react whenever booking data changes
 * (e.g. the dashboard's occupancy/revenue widgets) implements this and
 * registers with a {@link BookingSubject}.
 */
public interface DashboardObserver {
    void onBookingChanged();
}
