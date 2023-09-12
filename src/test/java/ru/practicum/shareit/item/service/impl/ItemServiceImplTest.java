package ru.practicum.shareit.item.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    private ItemDto itemDto;
    private ItemDto otherItemDto;
    private UserDto userDto;
    private UserDto otherUserDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@user.com");

        otherUserDto = new UserDto();
        otherUserDto.setName("otherUser");
        otherUserDto.setEmail("otherUser@user.com");

        itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        otherItemDto = new ItemDto();
        otherItemDto.setName("Отвертка");
        otherItemDto.setDescription("Аккумуляторная отвертка");
        otherItemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("Comment");
        commentDto.setAuthorName("User");
    }

    @Test
    @DisplayName("Создание вещи")
    void createItem() {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(1L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto actualItem = itemService.createItem(user.getId(), itemDto);

        assertEquals(itemDto.getName(), actualItem.getName(), "Имена не совпадают.");
        assertEquals(itemDto.getDescription(), actualItem.getDescription(), "Email не совпадают.");
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Создание вещи если id пользователя не существует")
    void addItemWithNotExistUser() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final UserNotFoundException e =
                assertThrows(UserNotFoundException.class, () -> itemService.createItem(userId, itemDto));
        assertEquals("Пользователя с id " + userId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи")
    void updateItem() {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(1L);
        item.setId(1L);
        item.setUser(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto actualItem = itemService.updateItem(user.getId(), item.getId(), otherItemDto);

        assertEquals(otherItemDto.getName(), actualItem.getName(),
                "Имена не совпадают.");
        assertEquals(otherItemDto.getDescription(), actualItem.getDescription(),
                "Email не совпадают.");
        assertEquals(otherItemDto.getAvailable(), actualItem.getAvailable(),
                "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи, если пользователя не существует")
    void updateItem_whenUserNotExist_thenReturnUserNotFoundException() {
        long userId = 1L;
        long itemId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final UserNotFoundException e =
                assertThrows(UserNotFoundException.class, () -> itemService.updateItem(userId, itemId, otherItemDto));
        assertEquals("Пользователя с id " + userId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи, если вещи не существует")
    void updateItem_whenItemNotExist_thenReturnItemNotFoundException() {
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(1L);
        long itemId = 1L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        final ItemNotFoundException e = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(user.getId(), itemId, otherItemDto));
        assertEquals("Вещи с id " + itemId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи с id другого пользователя")
    void updateItem_whenUserNotItemOwner_thenReturnItemOwnerException() {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        User otherUser = UserMapper.INSTANCE.toUser(otherUserDto);
        user.setId(1L);
        item.setId(1L);
        otherUser.setId(2L);
        item.setUser(otherUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        final ItemOwnerException e = assertThrows(ItemOwnerException.class,
                () -> itemService.updateItem(user.getId(), item.getId(), otherItemDto));
        assertEquals("Вещь не принадлежит пользователю с id " + user.getId(),
                e.getMessage());
    }

    @Test
    @DisplayName("Получение вещи пользователем, не являющимся владельцем")
    void getItem_whenUserNotOwner_thenReturnItemDto() {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setId(1L);
        item.setUser(new User(1L, "User", "user@user.ru"));
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        comment.setId(1L);
        ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
        itemOwnerDto.setComments(List.of(commentDto));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId())).thenReturn(List.of(comment));

        ItemDto actualItemDto = itemService.getItem(item.getId(), 2L);

        assertEquals(itemOwnerDto.getName(), actualItemDto.getName(),
                "Имена не совпадают.");
        assertEquals(itemOwnerDto.getDescription(), actualItemDto.getDescription(),
                "Email не совпадают.");
        assertEquals(itemOwnerDto.getAvailable(), actualItemDto.getAvailable(),
                "Доступности не совпадают.");
        assertEquals(itemOwnerDto.getComments(), List.of(commentDto));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(commentRepository, times(1)).findAllByItemId(item.getId());
    }

    @Test
    @DisplayName("Получение вещи пользователем, являющимся владельцем")
    public void getItem_whenUserIdItemOwner_thenReturnItemOwnerDtoWithBookings() {
        long itemId = 1L;
        long userId = 1L;
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setId(itemId);
        item.setUser(new User(1L, "User", "user@user.ru"));
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        List<Booking> lastBookings = new ArrayList<>();
        Booking lastBooking = new Booking();
        lastBookings.add(lastBooking);
        List<Booking> nextBookings = new ArrayList<>();
        Booking nextBooking = new Booking();
        nextBookings.add(nextBooking);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of(comment));
        when(bookingRepository.findFirstByItemIdAndEndDateBefore(itemId)).thenReturn(lastBookings);
        when(bookingRepository.findFirstByItemIdAndStartDateAfter(itemId)).thenReturn(nextBookings);

        ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
        itemOwnerDto.setComments(List.of(commentDto));
        itemOwnerDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
        itemOwnerDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
        itemOwnerDto.setComments(List.of(commentDto));

        ItemOwnerDto actualItemDto = (ItemOwnerDto) itemService.getItem(item.getId(), userId);

        assertEquals(itemOwnerDto.getName(), actualItemDto.getName(),
                "Имена не совпадают.");
        assertEquals(itemOwnerDto.getDescription(), actualItemDto.getDescription(),
                "Email не совпадают.");
        assertEquals(itemOwnerDto.getAvailable(), actualItemDto.getAvailable(),
                "Доступности не совпадают.");
        assertEquals(itemOwnerDto.getComments(), List.of(commentDto));
        assertNotNull(actualItemDto.getLastBooking());
        assertNotNull(actualItemDto.getNextBooking());

        verify(itemRepository, times(1)).findById(itemId);
        verify(commentRepository, times(1)).findAllByItemId(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndEndDateBefore(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartDateAfter(itemId);

    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void getUserAllItems() {
        long userId = 1L;
        int from = 1;
        int size = 1;
        long itemId = 1L;
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setId(itemId);
        User user = new User(1L, "User", "user@user.ru");
        item.setUser(user);
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        List<Booking> lastBookings = new ArrayList<>();
        Booking lastBooking = new Booking();
        lastBookings.add(lastBooking);
        List<Booking> nextBookings = new ArrayList<>();
        Booking nextBooking = new Booking();
        nextBookings.add(nextBooking);
        List<Item> items = List.of(item);
        ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
        itemOwnerDto.setComments(List.of(commentDto));
        itemOwnerDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
        itemOwnerDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
        itemOwnerDto.setComments(List.of(commentDto));
        List<ItemDto> itemDtos = List.of(itemOwnerDto);

        when(itemRepository.findAllByUserIdOrderByIdAsc(userId, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(items));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of(comment));
        when(bookingRepository.findFirstByItemIdAndEndDateBefore(itemId)).thenReturn(lastBookings);
        when(bookingRepository.findFirstByItemIdAndStartDateAfter(itemId)).thenReturn(nextBookings);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<ItemDto> listItems = itemService.getAllUserItems(userId, from, size);

        assertEquals(itemDtos.size(), listItems.size(), "Размер  списков не совпадает.");
        assertEquals(itemDtos.get(0).getId(), listItems.get(0).getId(), "Вещи не совпадают.");
        assertEquals(itemDtos.get(0).getName(), listItems.get(0).getName(), "Вещи не совпадают.");

        verify(itemRepository, times(1))
                .findAllByUserIdOrderByIdAsc(userId, PageRequest.of(from, size));
        verify(commentRepository, times(1)).findAllByItemId(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndEndDateBefore(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartDateAfter(itemId);
        verify(userRepository, times(1)).findById(userId);

    }

    @Test
    @DisplayName("Поиск вещей по пустой строке")
    void searchItems_whenSearchTextEmpty_thenReturnEmptyList() {
        long userId = 1L;
        String searchText = "";
        Integer from = 1;
        Integer size = 1;
        List<Item> items = Collections.emptyList();

        List<ItemDto> actualItems = itemService.searchItems(userId, searchText, from, size);

        assertEquals(0, actualItems.size());
    }

    @Test
    @DisplayName("Поиск вещей по запросу")
    void searchItems_whenSearchTextNotEmpty_thenReturnItemDtoList() {
        long userId = 1L;
        String searchText = "дРелЬ";
        int from = 1;
        int size = 1;

        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setId(1L);
        item.setUser(new User(1L, "User", "user@user.ru"));
        List<Item> items = List.of(item);

        when(itemRepository.findByUserAndNameOrDescription(userId, searchText, PageRequest.of(from, size)))
                .thenReturn(new PageImpl<>(items));

        List<ItemDto> actualItems = itemService.searchItems(userId, searchText, from, size);

        assertEquals(items.size(), actualItems.size());
        assertEquals(items.get(0).getName(), actualItems.get(0).getName());
        assertEquals(items.get(0).getDescription(), actualItems.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), actualItems.get(0).getAvailable());

        verify(itemRepository, times(1))
                .findByUserAndNameOrDescription(userId, searchText, PageRequest.of(from, size));

    }

    @Test
    @DisplayName("Публикация комментария пользователем, который арендовал вещь")
    void postComment_whenUserIsBooker_thenReturnCommentDto() {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(1L);
        item.setId(1L);
        List<Booking> bookings = new ArrayList<>();
        Booking booking = new Booking();
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookings.add(booking);
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        comment.setId(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndBookerId(item.getId(), user.getId()))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto actualCommentDto = itemService
                .postComment(user.getId(), item.getId(), CommentMapper.INSTANCE.toCommentDto(comment));

        assertEquals(comment.getId(), actualCommentDto.getId());
        assertEquals(comment.getText(), actualCommentDto.getText());
        assertEquals(comment.getAuthor().getName(), actualCommentDto.getAuthorName());
        assertEquals(comment.getCreated(), actualCommentDto.getCreated());

        verify(itemRepository, times(1)).findById(item.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(bookingRepository, times(1))
                .findAllByItemIdAndBookerId(item.getId(), user.getId());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Публикация комментария пользователем, который не арендовал вещь")
    void postComment_whenUserIsNotBooker_thenThrowItemBookerException() {
        long userId = 1L;
        long itemId = 2L;
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(userId);
        item.setId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndBookerId(itemId, userId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> itemService.postComment(userId, itemId, commentDto))
                .isInstanceOf(ItemBookerException.class)
                .hasMessageContaining(
                        String.format("Вещь с id %d не была арендована пользователем с id %d", itemId, userId));
    }

    @Test
    @DisplayName("Публикация комментария на несуществующую вещь")
    void postComment_whenItemDoesNotExist_thenThrowNotFoundException() {
        long userId = 1L;
        long itemId = 2L;
        User user = UserMapper.INSTANCE.toUser(userDto);
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.postComment(userId, itemId, commentDto))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining(String.format("Вещи с id %d нет в базе", itemId));
    }
}