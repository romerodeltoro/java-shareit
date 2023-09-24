package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Поле description не может быть пустым")
    private String description;
    private LocalDateTime created = LocalDateTime.now();
    private List<ItemDto> items = new ArrayList<>();

}
