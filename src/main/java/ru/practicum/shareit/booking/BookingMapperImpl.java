package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BookingMapperImpl implements BookingMapper{

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart().toString())
                .end(booking.getEnd().toString())
                .build();
    }

    @Override
    public BookingReplyDto toBookingReplyDto(Booking booking) {
        BookingReplyDto replyDto = new BookingReplyDto();
        replyDto.setId(booking.getId());
        replyDto.setStart(booking.getStart().toString());
        replyDto.setEnd(booking.getEnd().toString());
        replyDto.setStatus(String.valueOf(booking.getStatus()));
        replyDto.setBooker(UserMapper.INSTANCE.toUserBookingDto(booking.getBooker()));
        replyDto.setItem(ItemMapper.INSTANCE.toItemBookingDto(booking.getItem()));

        return replyDto;
    }

    @Override
    public Booking toBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }
        return Booking.builder()
                .id(bookingDto.getId())
                .start(LocalDateTime.parse(bookingDto.getStart(), formatter))
                .end(LocalDateTime.parse(bookingDto.getEnd(), formatter))
                .build();
    }
}
