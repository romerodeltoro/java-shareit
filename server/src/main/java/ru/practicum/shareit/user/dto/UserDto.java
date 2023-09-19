package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
@ToString
public class UserDto {

    private Long id;

    private String name;

    private String email;
}
