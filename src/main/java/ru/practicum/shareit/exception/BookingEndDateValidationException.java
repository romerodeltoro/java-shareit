package ru.practicum.shareit.exception;

public class BookingEndDateValidationException extends RuntimeException {
    public BookingEndDateValidationException(String message) {
        super(message);
    }
}
