package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @Autowired
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        if (itemDto.getId() == null) {
            itemDto.setId(0L);
        }
        userService.userExistCheck(userId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userMapper.toUser(userService.getUser(userId)));
        itemStorage.addItem(item);
        itemDto = itemMapper.toItemDto(item);
        log.info("Создана новая вещь - '{}'", itemDto);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        userService.userExistCheck(userId);
        itemExistCheck(itemId);
        itemOwnerCheck(userId, itemId);
        Item updateItem = itemStorage.getItem(itemId);
        updateItem.setName(itemDto.getName() != null ? itemDto.getName() : updateItem.getName());
        updateItem.setDescription(
                itemDto.getDescription() != null ? itemDto.getDescription() : updateItem.getDescription()
        );
        updateItem.setAvailable(
                itemDto.getAvailable() != null ? itemDto.getAvailable() : updateItem.getAvailable()
        );
        itemDto = itemMapper.toItemDto(updateItem);
        log.info("Вещь '{}' - обновлена", itemDto);

        return itemDto;
    }

    @Override
    public ItemDto getItem(long itemId) {
        itemExistCheck(itemId);
        log.info("Получена вещь с id '{}'", itemId);
        return itemMapper.toItemDto(itemStorage.getItem(itemId));
    }

    @Override
    public List<ItemDto> getAllUserItems(long userId) {
        userService.userExistCheck(userId);
        log.info("Получен список вещей пользователя с id '{}'", userId);
        return itemStorage.getAllItems().stream()
                .filter(i -> i.getOwner().getId() == userId)
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isEmpty()) {
            log.info("Не было найдено ни одного предмета по запросу '{}'", text);
            return Collections.emptyList();
        }
        log.info("Получен список вещей по запросу '{}'", text);
        return itemStorage.getAllItems().stream()
                .filter(i -> i.getDescription().toLowerCase().contains(text.toLowerCase()) |
                        i.getName().toLowerCase().contains(text.toLowerCase()))
                .filter(i -> i.getAvailable().equals(true))
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void itemExistCheck(long itemId) {
        Optional.ofNullable(itemStorage.getItem(itemId))
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещи с id %d нет в базе", itemId)));
    }

    private void itemOwnerCheck(long userId, long itemId) {
        if (itemStorage.getItem(itemId).getOwner().getId() != userId) {
            throw new ItemOwnerException(
                    String.format("Вещь с id %d не принадлежит пользователю с id %d", itemId, userId));
        }
    }

}
