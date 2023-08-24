package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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

    private final UserDto userDto = new UserDto();

    @BeforeEach
    void initial() {
        userDto.setName("User");
        userDto.setEmail("user@user.com");
    }

    @Test
    @DisplayName("Создание пользователя")
    void addUser() {
        final UserDto createdUser = userController.createUser(userDto).getBody();
        final long id = createdUser.getId();

        assertEquals(createdUser.getName(),
                userController.getUser(id).getBody().getName(), "Имена не совпадают.");
        assertEquals(createdUser.getEmail(),
                userController.getUser(id).getBody().getEmail(), "Email не совпадают.");
    }

    @Test
    @DisplayName("Создание пользователя с занятым email")
    void addUserWithSameEmail() {
        userController.createUser(userDto);
        final UserDto userWithSameEmail = new UserDto();
        userWithSameEmail.setName("User");
        userWithSameEmail.setEmail("user@user.com");

        final Throwable e = assertThrows(
                Throwable.class,
                () -> userController.createUser(userWithSameEmail)
        );
        assertTrue(e instanceof DataIntegrityViolationException);

    }

    @Test
    @DisplayName("Создание пользователя без email")
    void addUserWithOutEmail() {
        UserDto userWithOutEmail = new UserDto();
        userWithOutEmail.setName("User");

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
        UserDto userWithWrongEmail = new UserDto();
        userWithWrongEmail.setName("User");
        userWithWrongEmail.setEmail("user.com");

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
        final UserDto userWithName = new UserDto();
        userWithName.setName("updateName");
        final UserDto updatedUser = userController.updateUser(id, userWithName).getBody();
        final String updatedName = updatedUser.getName();

        assertEquals(userController.getUser(id).getBody().getName(),
                updatedName, "Имена не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя - только почта")
    void updateUserOnlyEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto userWithEmail = new UserDto();
        userWithEmail.setEmail("updateName@user.com");
        final UserDto updatedUser = userController.updateUser(id, userWithEmail).getBody();
        final String updatedEmail = updatedUser.getEmail();

        assertEquals(userController.getUser(id).getBody().getEmail(),
                updatedEmail, "Почты не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя такой же почтой")
    void updateUserWithSameEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto userWithEmail = new UserDto();
        userWithEmail.setEmail("user@user.com");
        final UserDto updatedUser = userController.updateUser(id, userWithEmail).getBody();

        assertEquals(userController.getUser(id).getBody().getName(),
                updatedUser.getName(), "Имена не совпадают.");
        assertEquals(userController.getUser(id).getBody().getEmail(),
                updatedUser.getEmail(), "Email не совпадают.");
    }

    @Test
    @DisplayName("Обновление пользователя уже занятой почтой")
    void updateUserWithExistEmail() {
        final long id = userController.createUser(userDto).getBody().getId();
        final UserDto createdUser = new UserDto();
        createdUser.setName("User");
        createdUser.setEmail("newUser@user.com");
        userController.createUser(createdUser);
        final UserDto userWithExistEmail = new UserDto();
        userWithExistEmail.setEmail("newUser@user.com");

        final UserEmailAlreadyExistException e = assertThrows(
                UserEmailAlreadyExistException.class,
                () -> userController.updateUser(id, userWithExistEmail)
        );
        assertEquals("Email newUser@user.com уже существует", e.getMessage());
    }

    @Test
    @DisplayName("Получение пользователя")
    void getUser() {
        final UserDto createdUser = userController.createUser(userDto).getBody();
        final long id = createdUser.getId();
        final UserDto receivedUser = userController.getUser(id).getBody();

        assertEquals(receivedUser.getName(),
                createdUser.getName(), "Имена не совпадают.");
        assertEquals(receivedUser.getEmail(),
                createdUser.getEmail(), "Email не совпадают.");
    }

    @Test
    @DisplayName("Получение списка пользователей")
    void getAllUsers() {
        userController.createUser(userDto);
        final UserDto createdUser = new UserDto();
        createdUser.setName("User");
        createdUser.setEmail("newUser@user.com");
        userController.createUser(createdUser);
        final List<UserDto> users = userController.getAllUsers().getBody();

        assertEquals(users.size(),
                userController.getAllUsers().getBody().size(), "Списки не совпадают.");
        assertEquals(users.get(0).getName(),
                userController.getAllUsers().getBody().get(0).getName(), "Пользователи не совпадают.");
        assertEquals(users.get(0).getEmail(),
                userController.getAllUsers().getBody().get(0).getEmail(), "Пользователи не совпадают.");
        assertEquals(users.get(1).getName(),
                userController.getAllUsers().getBody().get(1).getName(), "Пользователи не совпадают.");
        assertEquals(users.get(1).getEmail(),
                userController.getAllUsers().getBody().get(1).getEmail(), "Пользователи не совпадают.");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        UserDto createdUser = userController.createUser(userDto).getBody();
        userController.deleteUser(createdUser.getId());

        assertTrue(userController.getAllUsers().getBody().isEmpty());
    }

}
