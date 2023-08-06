package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserDto {

    private long id;

    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;
}
