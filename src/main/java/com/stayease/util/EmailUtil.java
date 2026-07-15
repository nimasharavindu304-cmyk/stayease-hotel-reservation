package com.stayease.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Optional API integration named in the coursework brief: emails a booking
 * confirmation to the guest. Disabled unless smtp.enabled=true is set in
 * config.properties, so the rest of the application works fully without it.
 */
public final class EmailUtil {

    private EmailUtil() {
    }

    public static boolean isEnabled() {
        Properties props = loadProperties();
        return props != null && "true".equalsIgnoreCase(props.getProperty("smtp.enabled", "false"));
    }

    /** Sends a plain-text booking confirmation. Silently does nothing if smtp.enabled=false. */
    public static void sendBookingConfirmation(String toEmail, String guestName, int bookingId,
                                                 String roomNumber, String checkIn, String checkOut,
                                                 String totalAmount) throws MessagingException {
        if (!isEnabled() || toEmail == null || toEmail.isBlank()) {
            return;
        }
        Properties config = loadProperties();

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.host", config.getProperty("smtp.host"));
        mailProps.put("mail.smtp.port", config.getProperty("smtp.port"));

        String username = config.getProperty("smtp.username");
        String password = config.getProperty("smtp.password");

        Session session = Session.getInstance(mailProps, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("StayEase Hotel - Booking Confirmation #" + bookingId);
        message.setText("Dear " + guestName + ",\n\n"
                + "Your reservation is confirmed.\n\n"
                + "Booking ID: " + bookingId + "\n"
                + "Room: " + roomNumber + "\n"
                + "Check-in: " + checkIn + "\n"
                + "Check-out: " + checkOut + "\n"
                + "Total amount: Rs. " + totalAmount + "\n\n"
                + "Thank you for choosing StayEase Hotel.");

        Transport.send(message);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = EmailUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                return null;
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            return null;
        }
    }
}
