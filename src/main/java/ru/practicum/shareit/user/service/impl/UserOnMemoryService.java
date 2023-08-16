package ru.practicum.shareit.user.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserEmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class UserOnMemoryService {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    //@Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() == null) {
            userDto.setId(0L);
        }
        userEmailExistCheck(userDto.getId(), userDto);
        User user = userMapper.toUser(userDto);
        userStorage.addUser(user);
        userDto = userMapper.toUserDto(user);
        log.info("Создан новый пользователь: '{}'", userDto);
        return userDto;
    }

    //@Override
    public UserDto updateUser(long userId, UserDto userDto) {
//        User user = userStorage.getUser(userId).orElseThrow(() -> new UserNotFoundException(String.format(
//                "Пользователя с id %d нет в базе", userId)));
        userExistCheck(userId);
        userEmailExistCheck(userId, userDto);
        UserDto newUserDto = userMapper.toUserDto(userStorage.getUser(userId));
        newUserDto.setName(userDto.getName() != null ? userDto.getName() : newUserDto.getName());
        newUserDto.setEmail(userDto.getEmail() != null ? userDto.getEmail() : newUserDto.getEmail());
        userStorage.updateUser(userMapper.toUser(newUserDto));
        log.info("Пользователь '{}' - обновлен", newUserDto);
        return newUserDto;
    }

    //@Override
    public UserDto getUser(long userId) {
        return userMapper.toUserDto(userStorage.getUser(userId));
    }

    //@Override
    public void deleteUser(long userId) {
        userExistCheck(userId);
        userStorage.deleteUser(userId);
        log.info("Пользователь с id '{}' - удален", userId);
    }

    //@Override
    public List<UserDto> getAllUsers() {
        List<UserDto> usersDto = userStorage.getUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Получен список всех пользователей");
        return usersDto;
    }

    private boolean isEmailExist(long userId, UserDto userDto) {
        User findUser = userStorage.getUsers().stream()
                .filter(u -> u.getEmail().equals(userDto.getEmail()))
                .findFirst()
                .orElse(null);
        return findUser != null && findUser.getId() != userId;
    }

    public void userExistCheck(long userId) {
        if (userStorage.getUser(userId) == null) {
            throw new UserNotFoundException(String.format(
                    "Пользователя с id %d нет в базе", userId)
            );
        }
    }

    private void userEmailExistCheck(long userId, UserDto userDto) {
        if (isEmailExist(userId, userDto)) {
            throw new UserEmailAlreadyExistException(
                    String.format("Пользователь с email %s уже существует", userDto.getEmail())
            );
        }
    }
}
