/*
package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new ItemRequestDto();
        requestDto.setDescription("Description");
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание запроса на аренду")
    public void createItemRequest_whenItemRequestDtoValid_thenItemRequestCreated() {
        long userId = 1L;
        when(requestService.createItemRequest(anyLong(), any())).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(requestService).createItemRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание запроса на аренду")
    public void createItemRequest_whenItemRequestDtoNotValid_thenItemRequestCreated() {
        long userId = 1L;
        requestDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).createItemRequest(userId, requestDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех запросов пользователя")
    public void getAllUserItems_whenUserExists_thenItemsReturned() {
        long userId = 1L;
        when(requestService.getAllUserItemsRequests(userId)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(requestService).getAllUserItemsRequests(anyLong());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех запросов")
    void getAllItems_whenUserExists_thenItemRequestsReturned() {
        int from = 0;
        int size = 10;
        long userId = 1L;
        when(requestService.getAllItems(userId, from, size)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(requestService).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка запросов с параметрами по умолчанию")
    void getAllItems_whenParamsDefault_thenItemRequestsReturned() {
        int from = 0;
        int size = 10;
        long userId = 1L;
        when(requestService.getAllItems(userId, from, size)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(requestService).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка запросов с некорректным параметром from")
    void getAllItems_whenParamFromInvalid_thenItemRequestsReturned() {
        int from = -1;
        int size = 10;
        long userId = 1L;
        when(requestService.getAllItems(userId, from, size)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка запросов с некорректным параметром size")
    void getAllItems_whenParamSizeMinInvalid_thenItemRequestsReturned() {
        int from = 0;
        int size = 0;
        long userId = 1L;
        when(requestService.getAllItems(userId, from, size)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка запросов с некорректным параметром size")
    void getAllItems_whenParamSizeMaxInvalid_thenItemRequestsReturned() {
        int from = 0;
        int size = 999;
        long userId = 1L;
        when(requestService.getAllItems(userId, from, size)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение запроса")
    void getItemRequest_whenUserExistsAndRequestIdExists_thenItemRequestReturned() {
        long userId = 1L;
        long requestId = 1L;
        when(requestService.getItemRequest(userId, requestId)).thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(requestService).getItemRequest(userId, requestId);
    }
}*/
