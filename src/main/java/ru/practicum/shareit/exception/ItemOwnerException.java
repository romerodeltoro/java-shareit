package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ItemOwnerException extends RuntimeException {
    public ItemOwnerException(String message) {
        super(message);
    }
}