package ru.practicum.shareit.request;


import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {

    private long id;
    private String description;
    private User requestor;
    private LocalDateTime created;

    private static long nextId = 1;

    public ItemRequest(String description, User requestor, LocalDateTime created) {
        this.id = nextId++;
        this.description = description;
        this.requestor = requestor;
        this.created = created;
    }
}
