package com.stayease.service;

import com.stayease.dao.GuestDAO;
import com.stayease.dao.GuestDAOImpl;
import com.stayease.exception.DuplicateGuestException;
import com.stayease.model.Guest;

import java.sql.SQLException;

/** Thin business layer around {@link GuestDAO} that enforces the NIC/passport uniqueness rule. */
public class GuestService {

    private final GuestDAO guestDAO = new GuestDAOImpl();

    public Guest registerGuest(Guest guest) throws DuplicateGuestException, SQLException {
        if (guestDAO.findByNic(guest.getNicPassport()) != null) {
            throw new DuplicateGuestException(
                    "A guest with NIC/Passport " + guest.getNicPassport() + " is already registered.");
        }
        return guestDAO.create(guest);
    }

    public void updateGuest(Guest guest) throws DuplicateGuestException, SQLException {
        Guest existing = guestDAO.findByNic(guest.getNicPassport());
        if (existing != null && existing.getGuestId() != guest.getGuestId()) {
            throw new DuplicateGuestException(
                    "Another guest already uses NIC/Passport " + guest.getNicPassport() + ".");
        }
        guestDAO.update(guest);
    }

    public GuestDAO getGuestDAO() {
        return guestDAO;
    }
}
