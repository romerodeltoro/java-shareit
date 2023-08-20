package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.Booking;

public class BookingEndDateValidator {

    public void validate(Booking booking) {
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new IllegalArgumentException("The end date must be later than the start date.");
        }
    }
}
