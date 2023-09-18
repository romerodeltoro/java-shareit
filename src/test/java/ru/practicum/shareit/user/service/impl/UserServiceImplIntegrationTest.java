package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserEmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceImplIntegrationTest {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final UserServiceImpl userService;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@user.com");
    }

    @Test
    @DisplayName("Создание пользователя")
    public void createUser_whenUserFieldsValid_thenSaveUser() {
        UserDto savedUser = userService.createUser(userDto);

        assertThat(savedUser.getId(), notNullValue());
        assertEquals(userDto.getName(), savedUser.getName(), "Имена не совпадают.");
        assertEquals(userDto.getEmail(), savedUser.getEmail(), "Email не совпадают.");
    }

    @Test
    @DisplayName("Создание пользователя с занятым email")
    public void createUser_whenUserWithSameEmail_thenThrowException() {
        UserDto userWithSameEmail = new UserDto();
        userWithSameEmail.setName("OtherUser");
        userWithSameEmail.setEmail("user@user.com");

        userService.createUser(userDto);

        final Throwable e = assertThrows(Throwable.class, () -> userService.createUser(userWithSameEmail));
        assertTrue(e instanceof DataIntegrityViolationException);

    }

    @Test
    @DisplayName("Получение пользователя по существующему ID")
    public void getUser_whenUserIdExists_thenReturnUser() {
        User user = repository.save(mapper.toUser(userDto));

        UserDto actualUser = userService.getUser(user.getId());

        assertEquals(user.getId(), actualUser.getId());
        assertEquals(user.getName(), actualUser.getName());
        assertEquals(user.getEmail(), actualUser.getEmail());
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID")
    public void getUser_whenUserIdNotExists_thenThrowException() {
        long userId = 99L;
        repository.save(mapper.toUser(userDto));

        final UserNotFoundException e =
                assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));
        assertEquals("Пользователя с id " + userId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление пользователя с существующим ID")
    public void updateUser_whenUserIdExists_thenUpdateUser() {
        long userId = userService.createUser(userDto).getId();
        UserDto updatedUser = new UserDto();
        updatedUser.setName("NewName");
        updatedUser.setEmail("updated@email.com");

        UserDto actualUser = userService.updateUser(userId, updatedUser);

        assertEquals(userId, actualUser.getId(), "ID не совпадают.");
        assertEquals(updatedUser.getName(), actualUser.getName(), "Имена не совпадают.");
        assertEquals(updatedUser.getEmail(), actualUser.getEmail(), "Email не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя с несуществующим ID")
    public void updateUser_whenUserIdNotExists_thenThrowException() {
        long userId = 99L;
        userService.createUser(userDto);

        final UserNotFoundException e =
                assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, userDto));
        assertEquals("Пользователя с id " + userId + " нет в базе", e.getMessage());
    }

    @Test
    @DisplayName("Обновление пользователя с уже существующим email")
    public void updateUser_whenEmailAlreadyExists_thenThrowException() {
        userService.createUser(userDto);
        UserDto updatedUser = new UserDto();
        updatedUser.setName("NewName");
        updatedUser.setEmail("updated@email.com");
        long userId = userService.createUser(updatedUser).getId();
        String email = userDto.getEmail();
        updatedUser.setEmail(email);

        final UserEmailAlreadyExistException e =
                assertThrows(UserEmailAlreadyExistException.class, () -> userService.updateUser(userId, updatedUser));
        assertEquals("Email " + email + " уже существует", e.getMessage());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    public void getAllUsers() {
        userService.createUser(userDto);
        UserDto updatedUser = new UserDto();
        updatedUser.setName("NewName");
        updatedUser.setEmail("updated@email.com");
        userService.createUser(updatedUser);
        List<UserDto> sourceUsers = List.of(userDto, updatedUser);

        List<UserDto> actualUsers = userService.getAllUsers();

        assertThat(actualUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(actualUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }
}
