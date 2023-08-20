package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.user.dto.UserBookingDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReplyDto extends BookingDto{

    private long id;
    private String start;
    private String end;
    private UserBookingDto booker;
    private ItemBookingDto item;
    private String status;
}
