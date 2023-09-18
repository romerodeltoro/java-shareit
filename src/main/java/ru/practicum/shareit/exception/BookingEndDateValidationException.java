package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingEndDateValidationException extends RuntimeException {
    public BookingEndDateValidationException(String message) {
        super(message);
    }
}
