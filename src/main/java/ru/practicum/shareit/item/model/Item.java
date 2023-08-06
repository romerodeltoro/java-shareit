package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
public class Item {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    //private ItemRequest request;

}
