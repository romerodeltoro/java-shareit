package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.booking.dto.LastBookingDto;
import ru.practicum.shareit.booking.dto.NextBookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    Booking toBooking(BookingDto bookingDto);

    BookingReplyDto toBookingReplyDto(Booking booking);

    @Mapping(target = "bookerId", source = "booking.booker.id")
    LastBookingDto lastBookingDto(Booking booking);

    @Mapping(target = "bookerId", source = "booking.booker.id")
    NextBookingDto nextBookingDto(Booking booking);
}
