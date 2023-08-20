package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;

public interface BookingService {

    BookingDto createBooking(long userId, BookingDto bookingDto);

    BookingDto approvingBooking(long userId, long bookingId, boolean approved);
}
