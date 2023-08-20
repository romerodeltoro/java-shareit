package ru.practicum.shareit.booking.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private long id;

    @NotNull
    private String start;

    @NotNull
    private String end;

    private long itemId;

/*private String status;
    private UserBookingDto booker;
    private ItemBookingDto item;*/


    /*@AssertTrue(message = "Дата начала аренды должна быть раньше даты ее завершения")
    public boolean isStartBeforeEnd() {
        return start.isBefore(end);
    }

    @AssertTrue(message = "Дата начала аренды не должна совпадать с датой завершения")
    public boolean isStartNotEqualToEnd() {
        return start != end;
    }*/

}
