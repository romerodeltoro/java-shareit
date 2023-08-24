/*
package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.booking.dto.LastBookingDto;
import ru.practicum.shareit.booking.dto.NextBookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    @Override
    public BookingReplyDto toBookingReplyDto(Booking booking) {
        BookingReplyDto replyDto = new BookingReplyDto();
        replyDto.setId(booking.getId());
        replyDto.setStart(booking.getStart());
        replyDto.setEnd(booking.getEnd());
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
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    @Override
    public List<BookingReplyDto> toBookingReplyDtoList(Iterable<Booking> bookings) {
        List<BookingReplyDto> result = new ArrayList<>();
        for (Booking booking : bookings) {
            result.add(toBookingReplyDto(booking));
        }
        return result;
    }

    @Override
    public LastBookingDto lastBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return LastBookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    @Override
    public NextBookingDto nextBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return NextBookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

}
*/
