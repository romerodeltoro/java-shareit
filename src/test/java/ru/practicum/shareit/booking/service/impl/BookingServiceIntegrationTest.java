package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReplyDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceIntegrationTest {

    private final BookingServiceImpl bookingService;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private UserDto userDto = new UserDto();
    private UserDto otherUserDto = new UserDto();
    private UserDto strangerDto = new UserDto();
    private ItemDto itemDto = new ItemDto();
    private BookingDto bookingDto = new BookingDto();
    private BookingDto lastBookingDto = new BookingDto();
    private BookingDto nextBookingDto = new BookingDto();

    @BeforeEach
    void setUp() {
        userDto.setName("User");
        userDto.setEmail("user@user.com");
        otherUserDto.setName("OtherUser");
        otherUserDto.setEmail("otheruser@user.com");
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger@email.com");

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        bookingDto.setStart(LocalDateTime.now().minusMinutes(1));
        bookingDto.setEnd(LocalDateTime.now().plusMinutes(1));
        bookingDto.setItemId(1L);
        lastBookingDto.setStart(LocalDateTime.now().minusMinutes(10));
        lastBookingDto.setEnd(LocalDateTime.now().minusMinutes(2));
        lastBookingDto.setItemId(1L);
        nextBookingDto.setStart(LocalDateTime.now().plusMinutes(2));
        nextBookingDto.setEnd(LocalDateTime.now().plusMinutes(10));
        nextBookingDto.setItemId(1L);
    }

    @Test
    @DisplayName("Создание бронирования")
    void createBooking_whenBookingDataValid_thenBookingCreated() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);

        BookingReplyDto actualBooking = (BookingReplyDto) bookingService.createBooking(otherUser.getId(), bookingDto);

        assertNotNull(actualBooking.getId());
        assertEquals(bookingDto.getStart(), actualBooking.getStart());
        assertEquals(bookingDto.getEnd(), actualBooking.getEnd());
        assertEquals(bookingDto.getItemId(), actualBooking.getItem().getId());
        assertEquals("WAITING", actualBooking.getStatus());
    }

    @Test
    @DisplayName("Создание бронирования когда дата окончания не валидна")
    void createBooking_whenEndDateNotValid_thenException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        assertThrows(BookingEndDateValidationException.class,
                () -> bookingService.createBooking(otherUser.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования на вещь, которая принадлежит пользователю")
    void createBooking_whenBookedItemBelongToUser_thenNotFoundException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования на недоступную вещь")
    void createBooking_whenBookedItemNotAvailable_thenItemNotAvailableException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setAvailable(false);

        assertThrows(ItemNotAvailableException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования на несуществующую вещь")
    void createBooking_whenBookedItemNotExist_thenItemNotFoundException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));

        assertThrows(ItemNotFoundException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования не существующим пользователем")
    void createBooking_whenBookerNotExist_thenUserNotFoundException() {

        assertThrows(UserNotFoundException.class,
                () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    @DisplayName("Одобрение бронирования")
    void approvingBooking_whenBookingExistsAndStatusIsWaiting_thenBookingApproved() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");

        BookingReplyDto bookingReplyDto =
                (BookingReplyDto) bookingService.approvingBooking(
                        user.getId(), booking.getId(), true);

        assertEquals("APPROVED", bookingReplyDto.getStatus());
    }

    @Test
    @DisplayName("Одобрение бронирования, которое не находится в статусе ожидания")
    void approvingBooking_whenBookingStatusNotWaiting_thenException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("REJECTED");

        assertThrows(HttpMessageNotReadableException.class,
                () -> bookingService.approvingBooking(user.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда вещь не принадлежит пользователю")
    void approvingBooking_whenItemNotBelongUser_thenException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(otherUser);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.approvingBooking(user.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда пользователя не существует")
    void approvingBooking_whenUserNotExist_thenException() {
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.approvingBooking(1L,
                        booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда брони не существует")
    void approvingBooking_whenBookingNotExist_thenException() {

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.approvingBooking(1L, 1L, true));
    }

    @Test
    @DisplayName("Получение бронирования")
    void getBooking_whenBookingExistsAndUserHasAccess_thenBookingReturned() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");

        BookingDto bookingDto =
                bookingService.getBooking(otherUser.getId(), booking.getId());

        assertEquals(booking.getId(), bookingDto.getId());
    }

    @Test
    @DisplayName("Получение брони, к которой пользователь не имеет доступа")
    void getBooking_whenBookingExistsAndUserHasNoAccess_thenException() {
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        User stranger = userRepository.save(UserMapper.INSTANCE.toUser(strangerDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setBooker(otherUser);
        booking.setItem(item);

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBooking(stranger.getId(), booking.getId()));
    }

    @Test
    @DisplayName("Получение брони, когда пользователя не существует")
    void getBooking_whenUserNotExists_thenException() {
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(UserMapper.INSTANCE.toUser(otherUserDto));
        booking.setItem(item);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getBooking(1L,booking.getId()));
    }

    @Test
    @DisplayName("Получение списка всех бронирований пользователя")
    void getUserAllBooking_whenUserExists_thenBookingsReturned() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getUserAllBooking(otherUser.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка бронирований c неизвестным параметром")
    void getUserAllBooking_whenStateUnknown_thenException() {
        String state = "UNKNOWN";
        int from = 0;
        int size = 10;
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));

        assertThrows(UnknownStateException.class,
                () -> bookingService.getUserAllBooking(otherUser.getId(), state, from, size));
    }

    @Test
    @DisplayName("Получение списка всех бронирований с параметром без брони")
    void getUserAllBooking_whenStateIsPast_thenBookingsReturned() {
        String state = "PAST";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(lastBookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getUserAllBooking(otherUser.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований с параметром без брони")
    void getUserAllBooking_whenStateIsFuture_thenBookingsReturned() {
        String state = "FUTURE";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(nextBookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getUserAllBooking(otherUser.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований с параметром без брони")
    void getUserAllBooking_whenStateIsCurrent_thenBookingsReturned() {
        String state = "CURRENT";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getUserAllBooking(otherUser.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований с параметром без брони")
    void getUserAllBooking_whenStateIsWaiting_thenBookingsReturned() {
        String state = "WAITING";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getUserAllBooking(otherUser.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи")
    void getAllBookingByOwner_whenUserExists_thenBookingsReturned() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка бронирований вледельцем вещи c неизвестным параметром")
    void getAllBookingByOwner_whenStateUnknown_thenException() {
        String state = "UNKNOWN";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));

        assertThrows(UnknownStateException.class,
                () -> bookingService.getAllBookingByOwner(user.getId(), state, from, size));
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи с параметром без брони")
    void getAllBookingByOwner_whenStateIsPast_thenBookingsReturned() {
        String state = "PAST";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(lastBookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи с параметром без брони")
    void getAllBookingByOwner_whenStateIsFuture_thenBookingsReturned() {
        String state = "FUTURE";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(nextBookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи с параметром без брони")
    void getAllBookingByOwner_whenStateIsCurrent_thenBookingsReturned() {
        String state = "CURRENT";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи с параметром без брони")
    void getAllBookingByOwner_whenStateIsWaiting_thenBookingsReturned() {
        String state = "WAITING";
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtoList.size());
    }

    @Test
    @DisplayName("Получение списка всех бронирований вледельцем вещи с параметром без брони")
    void getAllBookingByOwner_whenSize2_thenBookingsReturned() {
        String state = "ALL";
        int from = 2;
        int size = 1;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        Booking lastBooking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(lastBookingDto));
        Booking nextBooking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(nextBookingDto));
        booking.setItem(item);
        lastBooking.setItem(item);
        nextBooking.setItem(item);
        booking.setBooker(otherUser);
        lastBooking.setBooker(otherUser);
        nextBooking.setBooker(otherUser);

        List<BookingDto> bookingDtoList = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(1, bookingDtoList.size());
    }
}