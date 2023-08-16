package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserMapperTest {

    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final UserDto userDto = UserDto.builder().name("User").email("user@user.com").build();

    @Test
    void userMapperTest() {
        User user = userMapper.toUser(userDto);

        assertEquals(user.getName(), userDto.getName());
    }
}
