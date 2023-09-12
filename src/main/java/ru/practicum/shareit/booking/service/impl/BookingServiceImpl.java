package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


    @Override
    @Transactional
    public BookingDto createBooking(long userId, BookingDto bookingDto) {
        User user = ifUserExistReturnUser(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Вещи с id %d нет в базе", bookingDto.getItemId())));
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

        log.info("Пользователь '{}' создал запрос на бронь вещь - '{}'", user, item);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }

    /*@Override
    @Transactional
    public BookingDto updateBooking(long userId, BookingDto bookingDto) {
        User user = ifUserExistReturnUser(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(
                String.format("Вещи с id %d нет в базе", bookingDto.getItemId())));
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus("WAITING");
        item.setAvailable(false);

        log.info("Бронь '{}' обновлена", booking);
        return BookingMapper.INSTANCE.toBookingReplyDto(booking);
    }*/

    @Override
    @Transactional
    public BookingDto approvingBooking(long userId, long bookingId, boolean approved) {
        Booking booking = ifBookingExistReturnBooking(bookingId);
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Вещи с id %d нет в базе", (booking.getItem().getId()))));
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
    public List<BookingDto> getUserAllBooking(long userId, String state, int from, int size) {
        ifUserExistReturnUser(userId);

        List<Booking> bookings = getElementsFromPage(userId, state, from, size).getContent();

        log.info("Получен список бронирований с параметром '{}' пользователя с id '{}'", state, userId);
        return bookings.stream()
                .map(BookingMapper.INSTANCE::toBookingReplyDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingByOwner(long userId, String state, int from, int size) {
        ifUserExistReturnUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Booking> bookingPage = getBookingListForOwnerByState(userId, state, pageable);
        while (bookingPage.isEmpty()) {
            if (bookingPage.getPageable().hasPrevious()) {
                bookingPage = getBookingListForOwnerByState(userId, state, bookingPage.getPageable().previousOrFirst());
            } else {
                throw new NotFoundException("Бронирований нет");
            }
        }
        List<Booking> bookings = getBookingListForOwnerByState(userId, state, pageable).getContent();

        log.info("Получен список бронирований вещей пользователя с id '{}' с параметром '{}' ", userId, state);
        return bookings.stream()
                .map(BookingMapper.INSTANCE::toBookingReplyDto)
                .collect(Collectors.toList());

    }

    private Page<Booking> getElementsFromPage(long userId, String state, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Booking> bookingPage = getBookingListByState(userId, state, pageable);
        while (bookingPage.isEmpty()) {
            if (bookingPage.getPageable().hasPrevious()) {
                bookingPage = getBookingListByState(userId, state, bookingPage.getPageable().previousOrFirst());
            } else {
                throw new NotFoundException("Бронирований нет");
            }
        }
        return bookingPage;
    }

    private Page<Booking> getBookingListByState(long userId, String state, Pageable pageable) {

        switch (state) {
            case "ALL":
                return bookingRepository.findAllByBookerIdOrderByStartDateDesc(userId, pageable);

            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndDateBefore(userId, pageable);

            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartDateAfter(userId, pageable);

            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndDateBeforeAndDateAfter(userId, pageable);

            case "WAITING":
            case "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, state, pageable);

            default:
                throw new UnknownStateException(String.format("Unknown state: %s", state));
        }
    }

    private Page<Booking> getBookingListForOwnerByState(long userId, String state, Pageable pageable) {

        switch (state) {
            case "ALL":
                return bookingRepository.findAllByOwnerIdOrderByStartDateDesc(userId, pageable);

            case "PAST":
                return bookingRepository.findAllByOwnerIdAndEndDateBefore(userId, pageable);

            case "FUTURE":
                return bookingRepository.findAllByOwnerIdAndStartDateAfter(userId, pageable);

            case "CURRENT":
                return bookingRepository.findAllByOwnerIdAndDateBeforeAndDateAfter(userId, pageable);

            case "WAITING":
            case "REJECTED":
                return bookingRepository.findAllByOwnerIdAndStatusOrderByStartDateDesc(userId, state, pageable);

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
