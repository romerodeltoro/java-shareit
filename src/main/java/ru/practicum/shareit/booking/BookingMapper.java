package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.booking.dto.LastBookingDto;
import ru.practicum.shareit.booking.dto.NextBookingDto;

import java.util.List;

@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingDto toBookingDto(Booking booking);

    Booking toBooking(BookingDto bookingDto);

    BookingReplyDto toBookingReplyDto(Booking booking);

    List<BookingReplyDto> toBookingReplyDtoList(Iterable<Booking> bookings);

    LastBookingDto lastBookingDto(Booking booking);

    NextBookingDto nextBookingDto(Booking booking);
}
