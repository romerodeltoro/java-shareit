package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(long userId, BookingDto bookingDto);

    BookingDto updateBooking(long userId, BookingDto bookingDto);

    BookingDto approvingBooking(long userId, long bookingId, boolean approved);

    BookingDto getBooking(long userId, long bookingId);

    List<BookingDto> getUserAllBooking(long userId, String state);

    List<BookingDto> getAllBookingByOwner(long userId, String state);
}
