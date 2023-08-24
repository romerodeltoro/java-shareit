package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.user.dto.UserBookingDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingReplyDto extends BookingDto {

    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private UserBookingDto booker;
    private ItemBookingDto item;
    private String status;
}
