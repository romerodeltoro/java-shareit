package ru.practicum.shareit.item.dao.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryItemStorage implements ItemStorage {

    private Map<Long, Item> items = new HashMap<>();

    private static int incrementedUserId = 0;

    private long setIncrementedUserId() {
        return ++incrementedUserId;
    }

    @Override
    public Item addItem(Item item) {
        item.setId(setIncrementedUserId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItem(long id) {
        return items.get(id);
    }

    @Override
    public Item updateItem(Item item) {
        return items.put(item.getId(), item);
    }

    @Override
    public Collection<Item> getAllItems() {
        return items.values();
    }
}
