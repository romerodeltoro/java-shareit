package ru.practicum.shareit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class NotEqualToDateValidator implements ConstraintValidator<NotEqualToDate, LocalDateTime> {

    private LocalDateTime otherDate;

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {

        return value.isAfter(otherDate);
    }

}