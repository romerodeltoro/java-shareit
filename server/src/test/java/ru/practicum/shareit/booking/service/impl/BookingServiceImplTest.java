package ru.practicum.shareit.booking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    private UserDto userDto = new UserDto();
    private UserDto otherUserDto = new UserDto();
    private ItemDto itemDto = new ItemDto();
    private BookingDto bookingDto = new BookingDto();
    private BookingDto lastBookingDto = new BookingDto();
    private BookingDto nextBookingDto = new BookingDto();

    @BeforeEach
    void setUp() {
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@user.com");
        otherUserDto.setId(2L);
        otherUserDto.setName("OtherUser");
        otherUserDto.setEmail("otheruser@user.com");

        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        bookingDto.setId(2L);
        bookingDto.setStart(LocalDateTime.now().minusMinutes(1));
        bookingDto.setEnd(LocalDateTime.now().plusMinutes(1));
        bookingDto.setItemId(1L);
        lastBookingDto.setId(1L);
        lastBookingDto.setStart(LocalDateTime.now().minusMinutes(10));
        lastBookingDto.setEnd(LocalDateTime.now().minusMinutes(2));
        lastBookingDto.setItemId(1L);
        nextBookingDto.setId(3L);
        nextBookingDto.setStart(LocalDateTime.now().plusMinutes(2));
        nextBookingDto.setEnd(LocalDateTime.now().plusMinutes(10));
        nextBookingDto.setItemId(1L);
    }

    @Test
    @DisplayName("Создание бронирования")
    void createBooking_whenUserExists_thenBookingCreated() {
        User user = UserMapper.INSTANCE.toUser(userDto);
        User otherUser = UserMapper.INSTANCE.toUser(otherUserDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(otherUser);
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus("WAITING");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingReplyDto bookingReplyDto = (BookingReplyDto) bookingService.createBooking(user.getId(), bookingDto);

        assertEquals(booking.getId(), bookingReplyDto.getId());
        assertEquals(booking.getStart(), bookingReplyDto.getStart());
        assertEquals(booking.getEnd(), bookingReplyDto.getEnd());
        assertEquals(booking.getBooker().getId(), bookingReplyDto.getBooker().getId());
        assertEquals(booking.getItem().getName(), bookingReplyDto.getItem().getName());
        assertEquals(booking.getStatus(), bookingReplyDto.getStatus());
        verify(bookingRepository, times(1)).save(any());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Создание бронирования на вещь, которая принадлежит пользователю")
    void createBooking_whenBookedItemBelongToUser_thenNotFoundException() {
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования на недоступную вещь")
    void createBooking_whenBookedItemNotAvailable_thenItemNotAvailableException() {
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setAvailable(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ItemNotAvailableException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Создание бронирования на несуществующую вещь")
    void createBooking_whenBookedItemNotExist_thenItemNotFoundException() {
        User user = UserMapper.INSTANCE.toUser(userDto);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(ItemNotFoundException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));

    }

    @Test
    @DisplayName("Создание бронирования на существующую вещь")
    void createBooking_whenBookerNotExist_thenUserNotFoundException() {
        User user = UserMapper.INSTANCE.toUser(userDto);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    @DisplayName("Одобрение бронирования")
    void approvingBooking_whenBookingExistsAndApproved_thenBookingApproved() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);
        booking.setItem(item);
        booking.setStatus("WAITING");

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingReplyDto bookingReplyDto =
                (BookingReplyDto) bookingService.approvingBooking(
                        user.getId(), booking.getId(), true
                );

        assertEquals(booking.getId(), bookingReplyDto.getId());
        assertEquals(booking.getStart(), bookingReplyDto.getStart());
        assertEquals(booking.getEnd(), bookingReplyDto.getEnd());
        assertEquals(booking.getItem().getName(), bookingReplyDto.getItem().getName());
        assertEquals(booking.getStatus(), bookingReplyDto.getStatus());
        verify(bookingRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Одобрение бронирования, которое не находится в статусе ожидания")
    void approvingBooking_whenBookingStatusNotWaiting_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);
        booking.setItem(item);
        booking.setStatus("REJECTED");

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(HttpMessageNotReadableException.class,
                () -> bookingService.approvingBooking(user.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда вещь не принадлежит пользователю")
    void approvingBooking_whenItemNotBelongUser_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(UserMapper.INSTANCE.toUser(otherUserDto));
        booking.setItem(item);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.approvingBooking(user.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда пользователя не существует")
    void approvingBooking_whenUserNotExist_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(UserMapper.INSTANCE.toUser(otherUserDto));
        booking.setItem(item);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookingService.approvingBooking(item.getUser().getId(),
                        booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда вещи не существует")
    void approvingBooking_whenItemNotExist_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        booking.setItem(item);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> bookingService.approvingBooking(user.getId(),
                        booking.getId(), true));
    }

    @Test
    @DisplayName("Одобрение бронирования, когда брони не существует")
    void approvingBooking_whenBookingNotExist_thenException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.approvingBooking(userDto.getId(),
                        bookingDto.getId(), true));
    }

    @Test
    @DisplayName("Получение бронирования")
    void getBooking_whenBookingExistsAndUserHasAccess_thenBookingReturned() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);
        booking.setItem(item);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingReplyDto bookingReplyDto =
                (BookingReplyDto) bookingService.getBooking(user.getId(), booking.getId());

        assertEquals(booking.getId(), bookingReplyDto.getId());
        assertEquals(booking.getStart(), bookingReplyDto.getStart());
        assertEquals(booking.getEnd(), bookingReplyDto.getEnd());
        assertEquals(booking.getItem().getName(), bookingReplyDto.getItem().getName());
        verify(bookingRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Получение брони, к которой пользователь не имеет доступа")
    void getBooking_whenBookingExistsAndUserHasNoAccess_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(UserMapper.INSTANCE.toUser(otherUserDto));
        booking.setItem(item);
        booking.setBooker(UserMapper.INSTANCE.toUser(otherUserDto));

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBooking(userDto.getId(), bookingDto.getId()));
    }

    @Test
    @DisplayName("Получение брони, когда пользователя не существует")
    void getBooking_whenUserNotExists_thenException() {
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(UserMapper.INSTANCE.toUser(otherUserDto));
        booking.setItem(item);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getBooking(item.getUser().getId(), booking.getId()));
    }

    @Test
    @DisplayName("Получение брони, когда вещи не существует")
    void getBooking_whenBookingNotExists_thenException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBooking(userDto.getId(), bookingDto.getId()));
    }

    @Test
    @DisplayName("Получение списка всех бронирований пользователя")
    void getUserAllBooking_whenUserExists_thenBookingsReturned() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        User otherUser = UserMapper.INSTANCE.toUser(otherUserDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);
        Pageable pageable = PageRequest.of(from, size);

        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(bookingRepository.findAllByBookerIdOrderByStartDateDesc(otherUser.getId(), pageable))
                .thenReturn(new PageImpl<>(bookings, pageable, bookings.size()));

        List<BookingDto> bookingDtos = bookingService.getUserAllBooking(otherUserDto.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtos.size());
    }

    @Test
    @DisplayName("Получение списка бронирований c неизвестным параметром")
    void getUserAllBooking_whenStateUnknown_thenException() {
        String state = "SOMETHING";
        int from = 0;
        int size = 10;
        User otherUser = UserMapper.INSTANCE.toUser(otherUserDto);

        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        assertThrows(UnknownStateException.class,
                () -> bookingService.getUserAllBooking(otherUserDto.getId(), state, from, size));
    }

    @Test
    @DisplayName("Получение списка бронирований когда пользователя не существует")
    void getUserAllBooking_whenUserNotExists_thenException() {
        String state = "ALL";
        int from = 0;
        int size = 10;

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getUserAllBooking(userDto.getId(), state, from, size));
    }

    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи")
    void getAllBookingByOwner_whenUserExists_thenBookingsReturned() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        Booking booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        User otherUser = UserMapper.INSTANCE.toUser(otherUserDto);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setUser(user);
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus("WAITING");
        List<Booking> bookings = List.of(booking);
        Pageable pageable = PageRequest.of(from, size);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdOrderByStartDateDesc(user.getId(), pageable))
                .thenReturn(new PageImpl<>(bookings, pageable, bookings.size()));

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwner(user.getId(), state, from, size);

        assertEquals(bookings.size(), bookingDtos.size());
    }

    @Test
    @DisplayName("Получение списка бронирований c неизвестным параметром")
    void getAllBookingByOwner_whenStateUnknown_thenException() {
        String state = "SOMETHING";
        int from = 0;
        int size = 10;
        User user = UserMapper.INSTANCE.toUser(userDto);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(UnknownStateException.class,
                () -> bookingService.getAllBookingByOwner(userDto.getId(), state, from, size));
    }

    @Test
    @DisplayName("Получение списка бронирований когда пользователя не существует")
    void getAllBookingByOwner_whenUserNotExists_thenException() {
        String state = "ALL";
        int from = 0;
        int size = 10;

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllBookingByOwner(userDto.getId(), state, from, size));
    }
}
