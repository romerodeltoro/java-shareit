package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ResponseError>> notValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        final List<ResponseError> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ResponseError(error.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ResponseError>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage());
        final List<ResponseError> violations = e.getConstraintViolations().stream()
                .map(error -> new ResponseError(error.getMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(violations);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ResponseError> itemAlreadyExistException(ItemNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseError(e.getMessage()));
    }

    /*@ExceptionHandler(ItemNotAvailableException.class)
    public ResponseEntity<ResponseError> itemNotAvailableException(ItemNotAvailableException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseError(e.getMessage()));
    }*/

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    public ResponseEntity<ResponseError> userEmailAlreadyExistException(UserEmailAlreadyExistException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ResponseError(e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseError> userNotFoundException(UserNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseError(e.getMessage()));
    }

  /*  @ExceptionHandler(ItemOwnerException.class)
    public ResponseEntity<ResponseError> itemOwnerException(ItemOwnerException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseError(e.getMessage()));
    }*/

   /* @ExceptionHandler(BookingEndDateValidationException.class)
    public ResponseEntity<ResponseError> bookingEndDateValidationException(BookingEndDateValidationException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseError(e.getMessage()));
    }*/

  /*  @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ResponseError> bookingNotFoundException(BookingNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseError(e.getMessage()));
    }*/

    @ExceptionHandler(UnknownStateException.class)
    public ResponseEntity<ResponseError> unknownStateException(UnknownStateException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseError(e.getMessage()));
    }

   /* @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFoundException(NotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseError(e.getMessage()));
    }*/

 /*   @ExceptionHandler(ItemBookerException.class)
    public ResponseEntity<ResponseError> itemBookerException(ItemBookerException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseError(e.getMessage()));
    }*/

}
