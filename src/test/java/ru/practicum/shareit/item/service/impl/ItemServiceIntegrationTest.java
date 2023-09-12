package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ItemBookerException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
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
import java.util.List;

import static org.apache.logging.log4j.ThreadContext.isEmpty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceIntegrationTest {

    private final ItemServiceImpl itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

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
        commentDto.setAuthorName("otherUser");
    }

    @Test
    @DisplayName("Создание вещи")
    void createItem_thenItemIsCreated() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();

        ItemDto actualItem = itemService.createItem(userId, itemDto);

        assertEquals(itemDto.getName(),
                actualItem.getName(), "Имена не совпадают.");
        assertEquals(itemDto.getDescription(),
                actualItem.getDescription(), "Email не совпадают.");

    }

    @Test
    @DisplayName("Создание вещи если id пользователя не существует")
    void createItem_whenUserNotExist_thenThrowUserNotFoundException() {

        final UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemService.createItem(10, itemDto)
        );
        assertEquals("Пользователя с id 10 нет в базе", e.getMessage());
    }


    @Test
    @DisplayName("Обновление вещи")
    void updateItem_thenItemIsUpdated() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();

        ItemDto updatedItem = itemService.updateItem(userId, itemId, otherItemDto);

        assertEquals(otherItemDto.getName(),
                updatedItem.getName(), "Названия не совпадают.");
        assertEquals(otherItemDto.getDescription(),
                updatedItem.getDescription(), "Описания не совпадают.");
        assertEquals(otherItemDto.getAvailable(),
                updatedItem.getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи пользователем, который не является владельцем")
    void updateItem_whenUserExistsAndNotOwner_thenItemIsUpdated() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();
        long otherUserId = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto)).getId();

        assertThatThrownBy(() -> itemService.updateItem(otherUserId, itemId, otherItemDto))
                .isInstanceOf(ItemOwnerException.class)
                .hasMessageContaining(
                        String.format("Вещь не принадлежит пользователю с id %d", otherUserId));
    }

    @Test
    @DisplayName("Обновление вещи, если пользователя не существует")
    void updateItem_whenUserNotExist_thenReturnUserNotFoundException() {
        long userId = 1L;
        long itemId = 1L;

        final UserNotFoundException e =
                assertThrows(UserNotFoundException.class, () -> itemService.updateItem(userId, itemId, otherItemDto));
        assertEquals("Пользователя с id " + userId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи, если вещи не существует")
    void updateItem_whenItemNotExist_thenReturnItemNotFoundException() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = 1L;

        final ItemNotFoundException e = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(userId, itemId, otherItemDto));
        assertEquals("Вещи с id " + itemId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи - только available")
    void updateItemOnlyAvailable() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();
        ItemDto availableItem = new ItemDto();
        availableItem.setAvailable(false);

        ItemDto updatedItem = itemService.updateItem(userId, itemId, availableItem);

        assertEquals(itemDto.getName(),
                updatedItem.getName(), "Названия не совпадают.");
        assertEquals(itemDto.getDescription(),
                updatedItem.getDescription(), "Описания не совпадают.");
        assertEquals(availableItem.getAvailable(),
                updatedItem.getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только description")
    void updateItemOnlyDescription() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();
        ItemDto descriptionItem = new ItemDto();
        descriptionItem.setDescription("Аккумуляторная дрель + аккумулятор");

        ItemDto updatedItem = itemService.updateItem(userId, itemId, descriptionItem);

        assertEquals(itemDto.getName(),
                updatedItem.getName(), "Названия не совпадают.");
        assertEquals(descriptionItem.getDescription(),
                updatedItem.getDescription(), "Описания не совпадают.");
        assertEquals(itemDto.getAvailable(),
                updatedItem.getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только name")
    void updateItemOnlyName() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();
        ItemDto nameItem = new ItemDto();
        nameItem.setName("Аккумуляторная дрель");

        ItemDto updatedItem = itemService.updateItem(userId, itemId, nameItem);

        assertEquals(nameItem.getName(),
                updatedItem.getName(), "Названия не совпадают.");
        assertEquals(itemDto.getDescription(),
                updatedItem.getDescription(), "Описания не совпадают.");
        assertEquals(itemDto.getAvailable(),
                updatedItem.getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Получение вещи пользователем, который не является владельцем")
    void getItem_whenUserNotOwner_thenReturnItemOwnerDto() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = itemService.createItem(userId, itemDto).getId();
        long otherUserId = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto)).getId();

        ItemOwnerDto actualItem = (ItemOwnerDto) itemService.getItem(itemId, otherUserId);
        actualItem.setComments(List.of(commentDto));

        assertEquals(itemDto.getName(),
                actualItem.getName(), "Названия не совпадают.");
        assertEquals(itemDto.getDescription(),
                actualItem.getDescription(), "Описания не совпадают.");
        assertEquals(itemDto.getAvailable(),
                actualItem.getAvailable(), "Доступности не совпадают.");
        assertEquals(1, actualItem.getComments().size());
    }

    @Test
    @DisplayName("Получение вещи пользователем, который является владельцем")
    void getItem_whenUserOwner_thenReturnItemOwnerDtoWithBookings() {

        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.createItem(userId, itemDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));
        Comment comment = commentRepository.save(CommentMapper.INSTANCE.toComment(commentDto));
        comment.setItem(item);
        comment.setAuthor(otherUser);
        commentRepository.saveAndFlush(comment);
        List<Comment> comments = List.of(comment);

        BookingDto lastBookingDto = new BookingDto();
        lastBookingDto.setStart(LocalDateTime.now().minusDays(7));
        lastBookingDto.setEnd(LocalDateTime.now().minusDays(5));
        Booking lastBooking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(lastBookingDto));
        lastBooking.setBooker(otherUser);
        lastBooking.setItem(item);
        lastBooking.setStatus("APPROVED");
        bookingRepository.saveAndFlush(lastBooking);

        BookingDto nextBookingDto = new BookingDto();
        nextBookingDto.setStart(LocalDateTime.now().plusDays(5));
        nextBookingDto.setEnd(LocalDateTime.now().plusDays(7));
        Booking nextBooking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(nextBookingDto));
        nextBooking.setBooker(otherUser);
        nextBooking.setItem(item);
        nextBooking.setStatus("APPROVED");
        bookingRepository.saveAndFlush(nextBooking);

        ItemOwnerDto actualItem = (ItemOwnerDto) itemService.getItem(item.getId(), userId);

        assertEquals(itemDto.getName(),
                actualItem.getName(), "Названия не совпадают.");
        assertEquals(itemDto.getDescription(),
                actualItem.getDescription(), "Описания не совпадают.");
        assertEquals(itemDto.getAvailable(),
                actualItem.getAvailable(), "Доступности не совпадают.");
        assertEquals(comments.size(), actualItem.getComments().size());
        assertEquals(comments.get(0).getText(), actualItem.getComments().get(0).getText());
        assertEquals(lastBooking.getId(), actualItem.getLastBooking().getId());
        assertEquals(nextBooking.getId(), actualItem.getNextBooking().getId());

    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void getAllUserItems() {
        int from = 0;
        int size = 10;
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.createItem(userId, itemDto));

        List<ItemDto> actualItems = itemService.getAllUserItems(userId, from, size);

        assertEquals(1, actualItems.size());
        assertEquals(item.getId(), actualItems.get(0).getId());
        assertEquals(item.getName(), actualItems.get(0).getName());
        assertEquals(item.getDescription(), actualItems.get(0).getDescription());
        assertEquals(item.getAvailable(), actualItems.get(0).getAvailable());
    }

    @Test
    @DisplayName("Поиск вещей пользователем")
    void searchItems_whenUserExists_thenReturnItemsDto() {
        String searchText = "дРель";
        Integer from = 0;
        Integer size = 10;
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.createItem(userId, itemDto));

        List<ItemDto> itemsDto = itemService.searchItems(userId, searchText, from, size);

        assertEquals(1, itemsDto.size());
        assertEquals(item.getId(), itemsDto.get(0).getId());
        assertEquals(item.getName(), itemsDto.get(0).getName());
        assertEquals(item.getDescription(), itemsDto.get(0).getDescription());
        assertEquals(item.getAvailable(), itemsDto.get(0).getAvailable());
    }

    @Test
    @DisplayName("Поиск вещей пользователем с пустым запросом")
    void searchItems_whenUserExistsAndSearchTextIsEmpty_thenReturnEmptyList() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        itemService.createItem(userId, itemDto);
        String searchText = "";
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> itemsDto = itemService.searchItems(userId, searchText, from, size);

        assertThat(itemsDto.toString(), isEmpty());
    }

    @Test
    @DisplayName("Поиск несуществующих вещей")
    void searchItems_whenUserExistsAndSearchTextIsInvalid_thenReturnEmptyList() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        itemService.createItem(userId, itemDto);
        String searchText = "Новая вещь";
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> itemsDto = itemService.searchItems(userId, searchText, from, size);

        assertThat(itemsDto.toString(), isEmpty());
    }

    @Test
    @DisplayName("Добавление комментария пользователем, который арендовал вещь")
    void postComment_whenUserIsBooker_thenCommentIsPosted() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.createItem(userId, itemDto));
        User otherUser = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().minusDays(7));
        bookingDto.setEnd(LocalDateTime.now().minusDays(5));
        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toBooking(bookingDto));
        booking.setBooker(otherUser);
        booking.setItem(item);
        booking.setStatus("APPROVED");
        bookingRepository.saveAndFlush(booking);

        CommentDto actualComment =
                itemService.postComment(otherUser.getId(), item.getId(), commentDto);

        assertNotNull(actualComment);
        assertEquals(commentDto.getText(), actualComment.getText());
        assertEquals(commentDto.getAuthorName(), actualComment.getAuthorName());
        assertEquals(commentDto.getCreated(), actualComment.getCreated());

    }

    @Test
    @DisplayName("Добавление комментария пользователем, который не арендовал вещь")
    void postComment_whenUserIsNotBooker_thenThrowItemBookerException() {
        long userId = userRepository.save(UserMapper.INSTANCE.toUser(userDto)).getId();
        long itemId = ItemMapper.INSTANCE.toItem(itemService.createItem(userId, itemDto)).getId();
        long otherUserId = userRepository.save(UserMapper.INSTANCE.toUser(otherUserDto)).getId();

        assertThatThrownBy(() -> itemService.postComment(otherUserId, itemId, commentDto))
                .isInstanceOf(ItemBookerException.class)
                .hasMessageContaining(
                        String.format("Вещь с id %d не была арендована пользователем с id %d", itemId, otherUserId));

    }

}