package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.UserEmailAlreadyExistException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {


    private final UserController userController;

    private final UserDto userDto = UserDto.builder().name("User").email("user@user.com").build();


    @Test
    @DisplayName("Создание пользователя")
    void addUser() {
        final UserDto createdUser = userController.createUser(userDto).getBody();
        final long id = createdUser.getId();

        assertEquals(createdUser,
                userController.getUser(id).getBody(), "Пользователи не совпадают.");
    }

    @Test
    @DisplayName("Создание пользователя с занятым email")
    void addUserWithSameEmail() {
        userController.createUser(userDto);
        final UserDto userWithSameEmail = UserDto.builder().name("User").email("user@user.com").build();

        final UserEmailAlreadyExistException e = assertThrows(
                UserEmailAlreadyExistException.class,
                () -> userController.createUser(userWithSameEmail)
        );
        assertEquals("Пользователь с email user@user.com уже существует", e.getMessage());
    }

    @Test
    @DisplayName("Создание пользователя без email")
    void addUserWithOutEmail() {
        UserDto userWithOutEmail = UserDto.builder().name("User").build();

        final ValidationException e = assertThrows(
                ValidationException.class,
                () -> userController.createUser(userWithOutEmail)
        );
        assertEquals("Электронная почта не может быть пустой",
                e.getMessage().replace("createUser.userDto.email: ", ""));
    }

    @Test
    @DisplayName("Создание пользователя не корректным email")
    void addUserWithWrongEmail() {
        UserDto userWithWrongEmail = UserDto.builder().name("User").email("user.com").build();

        final ValidationException e = assertThrows(
                ValidationException.class,
                () -> userController.createUser(userWithWrongEmail)
        );
        assertEquals("Электронная почта должна содержать символ @",
                e.getMessage().replace("createUser.userDto.email: ", ""));
    }

    @Test
    @DisplayName("Обновление пользователя - только имя")
    void updateUserOnlyName() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto userWithName = UserDto.builder().name("updateName").build();
        final UserDto updatedUser = userController.updateUser(id, userWithName).getBody();
        final String updatedName = updatedUser.getName();

        assertEquals(userController.getUser(id).getBody().getName(),
                updatedName, "Имена не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя - только почта")
    void updateUserOnlyEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto userWithEmail = UserDto.builder().email("updateName@user.com").build();
        final UserDto updatedUser = userController.updateUser(id, userWithEmail).getBody();
        final String updatedEmail = updatedUser.getEmail();

        assertEquals(userController.getUser(id).getBody().getEmail(),
                updatedEmail, "Почты не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя такой же почтой")
    void updateUserWithSameEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto userWithEmail = UserDto.builder().email("user@user.com").build();
        final UserDto updatedUser = userController.updateUser(id, userWithEmail).getBody();

        assertEquals(userController.getUser(id).getBody(),
                updatedUser, "Пользователи не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя уже занятой почтой")
    void updateUserWithExistEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        userController.createUser(UserDto.builder().name("User").email("newUser@user.com").build());
        final UserDto userWithExistEmail = UserDto.builder().email("newUser@user.com").build();

        final UserEmailAlreadyExistException e = assertThrows(
                UserEmailAlreadyExistException.class,
                () -> userController.updateUser(id, userWithExistEmail)
        );
        assertEquals("Пользователь с email newUser@user.com уже существует", e.getMessage());
    }

    @Test
    @DisplayName("Получение пользователя")
    void getUser() {
        final UserDto createdUser = userController.createUser(userDto).getBody();
        final long id = createdUser.getId();
        final UserDto receivedUser = userController.getUser(id).getBody();

        assertEquals(receivedUser,
                createdUser, "Пользователи не совпадают.");
    }

    @Test
    @DisplayName("Получение списка пользователей")
    void getAllUsers() {
        userController.createUser(userDto);
        userController.createUser(UserDto.builder().name("User").email("newUser@user.com").build());
        final List<UserDto> users = userController.getAllUsers().getBody();

        assertEquals(users,
                userController.getAllUsers().getBody(), "Списки не совпадают.");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        UserDto createdUser = userController.createUser(userDto).getBody();
        userController.deleteUser(createdUser.getId());

        assertTrue(userController.getAllUsers().getBody().isEmpty());
    }
}
