package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemControllerTest {

    private final ItemController itemController;

    private final UserController userController;

    private final ItemDto itemDto =
            ItemDto.builder().name("Дрель").description("Простая дрель").available(true).build();
    private final ItemDto otherItemDto =
            ItemDto.builder().name("Отвертка").description("Аккумуляторная отвертка").available(true).build();
    private final UserDto userDto =
            UserDto.builder().name("User").email("user@user.com").build();
    private final UserDto otherUserDto =
            UserDto.builder().name("otherUser").email("otherUser@user.com").build();


    @Test
    @DisplayName("Создание вещи")
    void addItem() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto createdItem = itemController.createItem(userId, itemDto).getBody();
        final long id = createdItem.getId();

        assertEquals(createdItem,
                itemController.getItem(id).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Создание вещи если id пользователя не существует")
    void addItemWithNotExistUser() {

        final UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemController.createItem(10, itemDto)
        );
        assertEquals("Пользователя с id 10 нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Создание вещи если поле available отсутствует")
    void addItemWithoutAvailable() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto itemWithoutAvailable =
                ItemDto.builder().name("Дрель").description("Простая дрель").build();

        final ValidationException e = assertThrows(
                ValidationException.class,
                () -> itemController.createItem(userId, itemWithoutAvailable)
        );
        assertEquals("Поле available не может быть пустым",
                e.getMessage().replace("createItem.itemDto.available: ", ""));
    }

    @Test
    @DisplayName("Создание вещи если поле name пустое")
    void addItemWithEmptyName() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto itemWithEmptyName =
                ItemDto.builder().name("").description("Простая дрель").available(true).build();

        final ValidationException e = assertThrows(
                ValidationException.class,
                () -> itemController.createItem(userId, itemWithEmptyName)
        );
        assertEquals("Поле name не может быть пустым",
                e.getMessage().replace("createItem.itemDto.name: ", ""));
    }

    @Test
    @DisplayName("Создание вещи если поле available отсутствует")
    void addItemWithoutDescription() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto itemWithoutDescription =
                ItemDto.builder().name("Дрель").available(true).build();

        final ValidationException e = assertThrows(
                ValidationException.class,
                () -> itemController.createItem(userId, itemWithoutDescription)
        );
        assertEquals("Поле description не может быть пустым",
                e.getMessage().replace("createItem.itemDto.description: ", ""));
    }

    @Test
    @DisplayName("Обновление вещи")
    void updateItem() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto updatedItem =
                ItemDto.builder().id(itemId).name("Дрель+").description("Аккумуляторная дрель").available(false).build();

        assertEquals(updatedItem,
                itemController.updateItem(userId, itemId, updatedItem).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи с id другого пользователя")
    void updateItemWithOtherUser() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final long otherUserId = userController.createUser(otherUserDto).getBody().getId();

        final ItemOwnerException e = assertThrows(
                ItemOwnerException.class,
                () -> itemController.updateItem(otherUserId, itemId, itemDto)
        );
        assertEquals(String.format("Вещь с id %d не принадлежит пользователю с id %d", itemId, otherUserId), e.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи - только available")
    void updateItemOnlyAvailable() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto availableItem =
                ItemDto.builder().available(false).build();
        final ItemDto updatedItem =
                ItemDto.builder()
                        .id(itemId)
                        .name(itemDto.getName())
                        .description(itemDto.getDescription())
                        .available(availableItem.getAvailable())
                        .build();

        assertEquals(updatedItem,
                itemController.updateItem(userId, itemId, availableItem).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только description")
    void updateItemOnlyDescription() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto descriptionItem =
                ItemDto.builder().description("Аккумуляторная дрель + аккумулятор").build();
        final ItemDto updatedItem =
                ItemDto.builder()
                        .id(itemId)
                        .name(itemDto.getName())
                        .description(descriptionItem.getDescription())
                        .available(itemDto.getAvailable())
                        .build();

        assertEquals(updatedItem,
                itemController.updateItem(userId, itemId, descriptionItem).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только name")
    void updateItemOnlyName() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto nameItem =
                ItemDto.builder().name("Аккумуляторная дрель").build();
        final ItemDto updatedItem =
                ItemDto.builder()
                        .id(itemId)
                        .name(nameItem.getName())
                        .description(itemDto.getDescription())
                        .available(itemDto.getAvailable())
                        .build();

        assertEquals(updatedItem,
                itemController.updateItem(userId, itemId, nameItem).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Получение вещи")
    void getItem() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto item = itemController.createItem(userId, itemDto).getBody();

        assertEquals(item,
                itemController.getItem(item.getId()).getBody(), "Вещи не совпадают.");
    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void getUserAllItems() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto item = itemController.createItem(userId, itemDto).getBody();
        final ItemDto otherItem = itemController.createItem(userId, otherItemDto).getBody();
        final List<ItemDto> listItems = Arrays.asList(item, otherItem);

        assertEquals(listItems,
                itemController.getAllUserItems(userId).getBody(), "Списки не совпадают.");
    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void searchItems() {
       /* final long userId = userController.createUser(userDto).getBody().getId();
        itemController.createItem(userId, itemDto);
        final ItemDto otherItem = itemController.createItem(userId, otherItemDto).getBody();
        final List<ItemDto> listItems = Arrays.asList(otherItem);
        final String searchText = "аккУМУляторная";

        assertEquals(listItems,
                itemController.searchItems(searchText).getBody(), "Вещи не совпадают.");*/
    }
}
