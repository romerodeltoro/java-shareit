package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private final BookingRepository bookingRepository;


    private final ItemRepository itemRepository;


    private final UserRepository userRepository;


    private final ItemService itemService;


    @Override
    @Transactional
    public BookingDto createBooking(long userId, BookingDto bookingDto) {
        User user = ifUserExistReturnUser(userId);
        Item item = itemService.ifItemExistReturnItem(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException(String.format("Вещь %s не доступна для бронирования", item));
        }
        if (userId == item.getUser().getId()) {
            throw new NotFoundException(
                    String.format("Вещь %s не доступна для бронирования для пользователя %s", item, user));
        }
        endDateValidate(bookingDto);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus("WAITING");

        log.info("Пользователь '{}' создал запрос на бронь вещи - '{}'", user, item);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(long userId, BookingDto bookingDto) {
        User user = ifUserExistReturnUser(userId);
        Item item = itemService.ifItemExistReturnItem(bookingDto.getItemId());
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus("WAITING");
        item.setAvailable(false);

        log.info("Бронь '{}' обновлена", booking);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approvingBooking(long userId, long bookingId, boolean approved) {
        Booking booking = ifBookingExistReturnBooking(bookingId);
        Item item = itemService.ifItemExistReturnItem(booking.getItem().getId());
        ifUserExistReturnUser(userId);

        if (userId != item.getUser().getId()) {
            throw new BookingNotFoundException(
                    String.format("Вещь %s не принадлежит пользователю с id %d", booking.getItem(), userId));
        }
        if (booking.getStatus().equals("WAITING")) {
            booking.setStatus(approved ? "APPROVED" : "REJECTED");
        } else {
            throw new HttpMessageNotReadableException("Статус брони уже изменен");
        }
        log.info("Бронь с id - '{}' получила новый статус - '{}'", bookingId, booking.getStatus());
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    @Override
    public BookingDto getBooking(long userId, long bookingId) {
        Booking booking = ifBookingExistReturnBooking(bookingId);
        Item item = itemRepository.findById(booking.getItem().getId()).get();
        ifUserExistReturnUser(userId);

        if (userId != item.getUser().getId() && userId != booking.getBooker().getId()) {
            throw new BookingNotFoundException("У вас нет доступа к этой брони");
        }

        log.info("Получена бронь '{}'", booking);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    @Override
    public List<BookingDto> getUserAllBooking(long userId, String state) {
        ifUserExistReturnUser(userId);
        List<Booking> bookings = getBookingListByState(userId, state);

        log.info("Получен список бронирований с параметром '{}' пользователя с id '{}'", state, userId);
        return bookings.stream()
                .map(BookingMapper.INSTANCE::toBookingReplyDto)
                //.sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingByOwner(long userId, String state) {
        ifUserExistReturnUser(userId);
        List<Booking> bookings = getBookingListForOwnerByState(userId, state);

        log.info("Получен список бронирований вещей пользователя с id '{}' с параметром '{}' ", userId, state);
        return bookings.stream()
                .map(BookingMapper.INSTANCE::toBookingReplyDto)
                .collect(Collectors.toList());

    }

    private List<Booking> getBookingListByState(long userId, String state) {

        switch (state) {
            case "ALL":
                return bookingRepository.findAllByBookerIdOrderByStartDateDesc(userId);

            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndDateBefore(userId);

            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartDateAfter(userId);

            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndDateBeforeAndDateAfter(userId);

            case "WAITING":
            case "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, state);

            default:
                throw new UnknownStateException(String.format("Unknown state: %s", state));
        }
    }

    private List<Booking> getBookingListForOwnerByState(long userId, String state) {

        switch (state) {
            case "ALL":
                return bookingRepository.findAllByOwnerIdOrderByStartDateDesc(userId);

            case "PAST":
                return bookingRepository.findAllByOwnerIdAndEndDateBefore(userId);

            case "FUTURE":
                return bookingRepository.findAllByOwnerIdAndStartDateAfter(userId);

            case "CURRENT":
                return bookingRepository.findAllByOwnerIdAndDateBeforeAndDateAfter(userId);

            case "WAITING":
            case "REJECTED":
                return bookingRepository.findAllByOwnerIdAndStatusOrderByStartDateDesc(userId, state);

            default:
                throw new UnknownStateException(String.format("Unknown state: %s", state));
        }
    }

    private Booking ifBookingExistReturnBooking(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(
                String.format("Брони с id %d - не существует", bookingId)));
    }

    private void endDateValidate(BookingDto booking) {
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getEnd().equals(booking.getStart())) {
            throw new BookingEndDateValidationException(
                    String.format("Дата окончания бронирования %s должна быть позже даты начала %s",
                            booking.getEnd(), booking.getStart()));
        }
    }

    private User ifUserExistReturnUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("Пользователя с id %d нет в базе", userId)));
    }


}
