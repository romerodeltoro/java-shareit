package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Autowired
    private final ItemRepository itemRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @Autowired
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Transactional
    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = itemRepository.save(itemMapper.toItem(itemDto));
        item.setUser(user);
        log.info("Создана новая вещь - '{}'", item);

        return itemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = ifItemExistReturnItem(itemId);
        itemOwnerCheck(user.getId(), item.getUser().getId());
        item.setName(itemDto.getName() != null ? itemDto.getName() : item.getName());
        item.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : item.getDescription());
        item.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : item.getAvailable());
        log.info("Вещь '{}' - обновлена", item);

        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(long itemId) {
        Item item = ifItemExistReturnItem(itemId);
        log.info("Получена вещь с id '{}'", itemId);

        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllUserItems(long userId) {
        userService.ifUserExistReturnUser(userId);
        log.info("Получен список вещей пользователя с id '{}'", userId);

        return itemMapper.toItemDtoList(itemRepository.findByUserId(userId));
    }

    @Override
    public List<ItemDto> searchItems(long userId, String searchText) {
        if (searchText.isEmpty()) {
            log.info("Не было найдено ни одного предмета по запросу '{}'", searchText);
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.findByUserAndNameOrDescription(userId, searchText);

        return itemMapper.toItemDtoList(items);
    }

    private Item ifItemExistReturnItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(
                String.format("Вещи с id %d нет в базе", itemId)));
    }

    private void itemOwnerCheck(long userId, long itemId) {
        if (itemId != userId) {
            throw new ItemOwnerException(
                    String.format("Вещь с id %d не принадлежит пользователю с id %d", itemId, userId));
        }
    }
}
