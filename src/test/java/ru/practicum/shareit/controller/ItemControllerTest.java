package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemControllerTest {

    private final ItemController itemController;

    private final UserController userController;

    private final ItemRepository itemRepository;

    private final ItemDto itemDto = new ItemDto();
    private final ItemDto otherItemDto = new ItemDto();
    private final UserDto userDto = new UserDto();
    private final UserDto otherUserDto = new UserDto();


    @BeforeEach
    void initial() {

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        otherItemDto.setName("Отвертка");
        otherItemDto.setDescription("Аккумуляторная отвертка");
        otherItemDto.setAvailable(true);

        userDto.setName("User");
        userDto.setEmail("user@user.com");

        otherUserDto.setName("otherUser");
        otherUserDto.setEmail("otherUser@user.com");
    }

    @Test
    @DisplayName("Создание вещи")
    void addItem() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto createdItem = itemController.createItem(userId, itemDto).getBody();
        final long id = createdItem.getId();

        assertEquals(createdItem.getName(),
                itemController.getItem(userId, id)
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(createdItem.getDescription(),
                itemController.getItem(userId, id)
                        .getBody().getDescription(), "Описания не совпадат.");
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
        final ItemDto itemWithoutAvailable = new ItemDto();
        itemWithoutAvailable.setName("Дрель");
        itemWithoutAvailable.setDescription("Простая дрель");

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
        final ItemDto itemWithEmptyName = new ItemDto();
        itemWithEmptyName.setName("");
        itemWithEmptyName.setDescription("Простая дрель");
        itemWithEmptyName.setAvailable(true);

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
        final ItemDto itemWithoutDescription = new ItemDto();
        itemWithoutDescription.setName("Дрель");
        itemWithoutDescription.setAvailable(true);

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
        final ItemDto updatedItem = new ItemDto();
        updatedItem.setId(itemId);
        updatedItem.setName("Дрель+");
        updatedItem.setDescription("Аккумуляторная дрель");
        updatedItem.setAvailable(false);

        assertEquals(updatedItem.getName(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(updatedItem.getDescription(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getDescription(), "Описания не совпадают.");
        assertEquals(updatedItem.getAvailable(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getAvailable(), "Доступности не совпадают.");
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
        final ItemDto availableItem = new ItemDto();
        availableItem.setAvailable(false);
        final ItemDto updatedItem = new ItemDto();
        updatedItem.setId(itemId);
        updatedItem.setName(itemDto.getName());
        updatedItem.setDescription(itemDto.getDescription());
        updatedItem.setAvailable(availableItem.getAvailable());

        assertEquals(updatedItem.getName(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(updatedItem.getDescription(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getDescription(), "Описания не совпадают.");
        assertEquals(updatedItem.getAvailable(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только description")
    void updateItemOnlyDescription() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto descriptionItem = new ItemDto();
        descriptionItem.setDescription("Аккумуляторная дрель + аккумулятор");
        final ItemDto updatedItem = new ItemDto();
        updatedItem.setId(itemId);
        updatedItem.setName(itemDto.getName());
        updatedItem.setDescription(descriptionItem.getDescription());
        updatedItem.setAvailable(itemDto.getAvailable());

        assertEquals(updatedItem.getName(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(updatedItem.getDescription(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getDescription(), "Описания не совпадают.");
        assertEquals(updatedItem.getAvailable(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Обновление вещи - только name")
    void updateItemOnlyName() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final long itemId = itemController.createItem(userId, itemDto).getBody().getId();
        final ItemDto nameItem = new ItemDto();
        nameItem.setName("Аккумуляторная дрель");
        final ItemDto updatedItem = new ItemDto();
        updatedItem.setId(itemId);
        updatedItem.setName(nameItem.getName());
        updatedItem.setDescription(itemDto.getDescription());
        updatedItem.setAvailable(itemDto.getAvailable());

        assertEquals(updatedItem.getName(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(updatedItem.getDescription(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getDescription(), "Описания не совпадают.");
        assertEquals(updatedItem.getAvailable(),
                itemController.updateItem(userId, itemId, updatedItem)
                        .getBody().getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Получение вещи")
    void getItem() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto item = itemController.createItem(userId, itemDto).getBody();

        assertEquals(item.getName(),
                itemController.getItem(userId, item.getId())
                        .getBody().getName(), "Названия не совпадают.");
        assertEquals(item.getDescription(),
                itemController.getItem(userId, item.getId())
                        .getBody().getDescription(), "Описания не совпадают.");
        assertEquals(item.getAvailable(),
                itemController.getItem(userId, item.getId())
                        .getBody().getAvailable(), "Доступности не совпадают.");
    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void getUserAllItems() {
        final long userId = userController.createUser(userDto).getBody().getId();
        final ItemDto item = itemController.createItem(userId, itemDto).getBody();
        final ItemDto otherItem = itemController.createItem(userId, otherItemDto).getBody();
       /* final List<Item> listItems = itemRepository.findAllByUserIdOrderByIdAsc(userId);
        final List<ItemOwnerDto> itemOwnerDtos =
                listItems.stream().map(ItemMapper.INSTANCE::toItemOwnerDto).collect(Collectors.toList());

        assertEquals(itemOwnerDtos.size(),
                itemController.getAllUserItems(userId).getBody().size(), "Размер  списков не совпадает.");
        assertEquals(itemOwnerDtos.get(0).getId(),
                itemController.getAllUserItems(userId).getBody().get(0).getId(), "Вещи не совпадают.");
        assertEquals(itemOwnerDtos.get(1).getId(),
                itemController.getAllUserItems(userId).getBody().get(1).getId(), "Вещи не совпадают.");
        assertEquals(itemOwnerDtos.get(0).getName(),
                itemController.getAllUserItems(userId).getBody().get(0).getName(), "Вещи не совпадают.");
        assertEquals(itemOwnerDtos.get(1).getName(),
                itemController.getAllUserItems(userId).getBody().get(1).getName(), "Вещи не совпадают.");*/
    }

    @Test
    @DisplayName("Поиск вещей по запросу")
    void searchItems() {
        final long userId = userController.createUser(userDto).getBody().getId();
        itemController.createItem(userId, itemDto);
        final ItemDto otherItem = itemController.createItem(userId, otherItemDto).getBody();
        final List<ItemDto> listItems = Arrays.asList(otherItem);
        final String searchText = "аккУМУляторная";

        assertEquals(listItems.size(),
                itemController.searchItems(userId, searchText)
                        .getBody().size(), "Размер  списков не совпадает.");
        assertEquals(listItems.get(0).getName(),
                itemController.searchItems(userId, searchText)
                        .getBody().get(0).getName(), "Вещи не совпадают.");
        assertEquals(listItems.get(0).getDescription(),
                itemController.searchItems(userId, searchText)
                        .getBody().get(0).getDescription(), "Вещи не совпадают.");
    }
}
