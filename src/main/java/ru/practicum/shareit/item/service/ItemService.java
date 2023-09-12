package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItem(long itemId, long userId);

    List<ItemDto> getAllUserItems(long userId);

    List<ItemDto> searchItems(long userId, String text);

    Item ifItemExistReturnItem(long itemId);

    CommentDto postComment(long userId, long itemId, CommentDto commentDto);

}
