package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper
public interface ItemMapper {

    ItemDto toItemDto(Item item);

    public Item toItem(ItemDto itemDto);

    List<ItemDto> toItemDtoList(Iterable<Item> items);
}
