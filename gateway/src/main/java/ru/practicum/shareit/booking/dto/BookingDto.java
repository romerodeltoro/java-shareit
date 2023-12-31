package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {

    private Long id;

    @NotNull
    @FutureOrPresent(message = "Дата начала аренды не должна быть в прошлом")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Дата завершения аренды не должна быть в прошлом")
    private LocalDateTime end;

    private long itemId;
}
