package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item addItem(Item item);

    Item getItem(long id);

    Item updateItem(Item item);

    Collection<Item> getAllItems();
}
