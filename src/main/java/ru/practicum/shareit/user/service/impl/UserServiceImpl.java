package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserEmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository repository;

    @Autowired
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = repository.save(userMapper.toUser(userDto));
        log.info("Создан новый пользователь: '{}'", user);
        return userMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        User user = ifUserExistReturnUser(userId);
        isEmailExist(userId, userDto.getEmail());
        user.setName(userDto.getName() != null ? userDto.getName() : user.getName());
        user.setEmail(userDto.getEmail() != null ? userDto.getEmail() : user.getEmail());
        repository.saveAndFlush(user);
        log.info("Пользователь '{}' - обновлен", user);
        return userMapper.toUserDto(user);
    }

    //@Transactional(readOnly = true)
    @Override
    public UserDto getUser(long userId) {
        User user = ifUserExistReturnUser(userId);
        log.info("Получен пользователь '{}'", user);
        return userMapper.toUserDto(user);
    }


    @Transactional
    @Override
    public void deleteUser(long userId) {
        repository.deleteById(userId);
        log.info("Пользователь с id '{}' - удален", userId);
    }

    //@Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toUserDtoList(repository.findAll());
    }

    public User ifUserExistReturnUser(long userId) {
        return repository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("Пользователя с id %d нет в базе", userId)));
    }


    private void isEmailExist(long userId, String email) {
        Optional<User> user = repository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        } else if (user.get().getId() != userId) {
            throw new UserEmailAlreadyExistException(
                    String.format("Email %s уже существует", email)
            );
        }
    }

}
