package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.UserEmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDto userDto;


    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@user.com");
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание пользователя")
    public void createUser_whenUserIsValid_thenUserCreated() {

        when(userService.createUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание пользователя с пустым полем name")
    public void createUser_whenUserWithBlankName_thenReturnedBadRequest() {
        final UserDto userDtoWithBlankName = new UserDto();
        userDtoWithBlankName.setEmail("user@user.com");
        when(userService.createUser(userDtoWithBlankName)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDtoWithBlankName))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(userDtoWithBlankName);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание пользователя с пустым полем email")
    public void createUser_whenUserWithBlankEmail_thenReturnedBadRequest() {
        final UserDto userDtoWithBlankEmail = new UserDto();
        userDtoWithBlankEmail.setEmail("User");
        when(userService.createUser(userDtoWithBlankEmail)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDtoWithBlankEmail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(userDtoWithBlankEmail);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание пользователя с не корректным email")
    public void createUser_whenUserWithWrongEmail_thenReturnedBadRequest() {
        final UserDto userWithWrongEmail = new UserDto();
        userWithWrongEmail.setName("User");
        userWithWrongEmail.setEmail("user.com");
        when(userService.createUser(userWithWrongEmail)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userWithWrongEmail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(userWithWrongEmail);
    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление пользователя - только имя")
    public void updateUser_whenUserOnlyName_thenUserUpdated() {
        final Long userId = 1L;
        userDto.setName("updateName");
        UserDto updatedUser = makeUserDto(userId, userDto.getName(), userDto.getEmail());


        when(userService.updateUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление пользователя - только email")
    public void updateUser_whenUserOnlyEmail_thenUserUpdated() {
        final Long userId = 1L;
        userDto.setEmail("update@user.com");
        UserDto updatedUser = makeUserDto(userId, userDto.getName(), userDto.getEmail());

        when(userService.updateUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление пользователя с такой же почтой")
    public void updateUser_whenUserWithSameEmail_thenUserUpdated() {
        final Long userId = 1L;
        userDto.setName("updateName");
        userDto.setEmail("user@user.com");
        UserDto updatedUser = makeUserDto(userId, userDto.getName(), userDto.getEmail());

        when(userService.updateUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление пользователя с уже занятой почтой")
    public void updateUser_whenUserWithExistEmail_thenUserEmailAlreadyExistExceptionTrows() {
        final Long userId = 1L;
        UserDto updatedUser = makeUserDto(userId, userDto.getName(), userDto.getEmail());

        when(userService.updateUser(any(), any())).thenThrow(UserEmailAlreadyExistException.class);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(userService, never()).updateUser(userId, updatedUser);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение пользователя")
    public void getUser() {
        final Long userId = 0L;

        mockMvc.perform(get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).getUser(userId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение несуществующего пользователя")
    public void getUser_whenIdNotExist_thenReturnUserNotFoundException() {
        long userId = 100L;
        when(userService.getUser(anyLong())).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка пользователея")
    public void getAllUsers() {
        when(userService.getAllUsers())
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));

        verify(userService).getAllUsers();
    }

    private UserDto makeUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);

        return dto;
    }

}