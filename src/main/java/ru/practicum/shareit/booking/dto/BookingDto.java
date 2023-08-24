package ru.practicum.shareit.booking.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private long id;

    @NotNull
    @FutureOrPresent(message = "Дата начала аренды не должна быть в прошлом")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Дата завершения аренды не должна быть в прошлом")
    private LocalDateTime end;

    private long itemId;


}
