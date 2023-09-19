package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class ItemDto {
    private Long id;

    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть пустым")
    private Boolean available;

    private Long requestId;


}
