package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.user.dto.UserBookingDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReplyDto extends BookingDto{

    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private UserBookingDto booker;
    private ItemBookingDto item;
    private String status;
}
