package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть пустым")
    private Boolean available;


}
