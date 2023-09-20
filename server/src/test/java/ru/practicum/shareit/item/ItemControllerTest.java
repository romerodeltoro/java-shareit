/*
package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.ValidationException;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;


    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание вещи")
    public void createItem_whenItemIsValid_thenItemCreated() {
        long userId = 1L;
        when(itemService.createItem(anyLong(), any())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService).createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание вещи пользователем с некорректным именем")
    void createItem_whenItemNameIsInvalid_thenItemNotCreated() {
        long userId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);
        when(itemService.createItem(userId, itemDto)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(userId, itemDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание вещи пользователем с некорректным описанием")
    void createItem_whenItemDescriptionIsInvalid_thenItemNotCreated() {
        long userId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setAvailable(true);
        when(itemService.createItem(userId, itemDto)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(userId, itemDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание вещи пользователем с некорректной доступностью")
    void createItem_whenItemAvailableIsInvalid_thenItemNotCreated() {
        long userId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        when(itemService.createItem(userId, itemDto)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(userId, itemDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление вещи пользователем, который является владельцем")
    void updateItem_whenUserIsOwner_thenItemUpdated() {
        long userId = 1L;
        long itemId = 2L;
        ItemDto updatedItem = makeItemDto(itemId, "Дрель+", "Аккумуляторная дрель", true);
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(updatedItem))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItem.getName())))
                .andExpect(jsonPath("$.description", is(updatedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItem.getAvailable())));

    }

    @SneakyThrows
    @Test
    @DisplayName("Обновление вещи без ID пользователя")
    void updateItem_whenUserIdAbsent_thenTrowUnknownStateException() {
        long userId = 1L;
        long itemId = 2L;
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenThrow(UnknownStateException.class);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).updateItem(userId, itemId, itemDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение вещи")
    void getItem_whenUserIsOwner_thenItemReturned() {
        long userId = 1L;
        long itemId = 2L;
        when(itemService.getItem(itemId, userId)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService).getItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение несуществующей вещи")
    public void getItem_whenIdNotExist_thenReturnItemNotFoundException() {
        long userId = 1L;
        long itemId = 100L;
        when(itemService.getItem(itemId, userId)).thenThrow(ItemNotFoundException.class);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка вещей пользователея")
    public void getAllUserItems() {
        long userId = 1L;
        int from = 0;
        int size = 10;
        when(itemService.getAllUserItems(userId, from, size))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));

        verify(itemService).getAllUserItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Поиск вещей по запросу")
    void searchItems_whenTextIsProvided_thenItemsReturned() {
        long userId = 1L;
        String searchText = "дРеЛ";

        when(itemService.searchItems(userId, searchText, 0, 10)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", searchText)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));

        verify(itemService).searchItems(userId, searchText, 0, 10);
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавление комментария пользователем")
    void postComment_whenUserExists_thenCommentAdded() {
        long userId = 1L;
        long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отзыв о товаре");

        Mockito.when(itemService.postComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }

    private ItemDto makeItemDto(Long id, String name, String description, Boolean available) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);

        return dto;
    }

}*/
