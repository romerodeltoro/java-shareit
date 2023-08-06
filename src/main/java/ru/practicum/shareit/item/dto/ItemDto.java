package ru.practicum.shareit.item.dto;


import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemDto {
    private long id;

    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть пустым")
    private Boolean available;

    //private ItemRequest request;

}
