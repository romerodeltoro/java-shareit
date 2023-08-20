package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.item.mapper.ItemMapper;

@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingDto toBookingDto(Booking booking);

    Booking toBooking(BookingDto bookingDto);

    BookingReplyDto toBookingReplyDto(Booking booking);
}
