package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BookingEndDateValidationException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private final ItemService itemService;


    @Override
    @Transactional
    public BookingDto createBooking(long userId, BookingDto bookingDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = itemService.ifItemExistReturnItem(bookingDto.getItemId());
        if(!item.getAvailable()) {
            throw new ItemNotAvailableException(String.format("Вещь %s не доступна для бронирования", item));
        }
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        endDateValidate(booking);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Booking.Status.WAITING);
        item.setAvailable(false);

        log.info("Пользователь '{}' создал запрос на бронь вещи - '{}'", user, item);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approvingBooking(long userId, long bookingId, boolean approved) {
        Booking booking = ifBookingExistReturnBooking(bookingId);
        Item item = itemService.ifItemExistReturnItem(booking.getItem().getId());
        userService.ifUserExistReturnUser(userId);

        if (userId != item.getUser().getId()) {
            throw new RuntimeException(
                    String.format("Вещь %s не принадлежит пользователю с id %d", booking.getItem(), userId));
        }
        booking.setStatus(approved ? Booking.Status.APPROVED : Booking.Status.REJECTED);

        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    private Booking ifBookingExistReturnBooking(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(
                String.format("Брони с id %d - не существует", bookingId)));
    }

    private void endDateValidate(Booking booking) {
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getEnd().equals(booking.getStart())) {
            throw new BookingEndDateValidationException(
                    String.format("Дата окончания бронирования %s должна быть позже даты начала %s",
                            booking.getEnd(), booking.getStart()));
        }
    }
}
