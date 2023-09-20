/*
package ru.practicum.shareit.booking;



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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusMinutes(1).withNano(0));
        bookingDto.setEnd(LocalDateTime.now().plusMinutes(2));
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание бронирования")
    void createBooking_whenBookingDataValid_thenBookingCreated() {
        long userId = 1L;
        when(bookingService.createBooking(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).createBooking(anyLong(), any());
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание бронирования с валидной датой начала")
    void createBooking_whenStartIsInvalid_thenException() {
        long userId = 1L;
        bookingDto.setStart(LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(userId, bookingDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Создание бронирования с валидной датой конца")
    void createBooking_whenEndIsInvalid_thenException() {
        long userId = 1L;
        bookingDto.setEnd(LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(userId, bookingDto);
    }

    @SneakyThrows
    @Test
    @DisplayName("Утверждение бронирования")
    void approvingBooking_whenBookingExistsAndStatusIsWaiting_thenBookingApproved() {
        long userId = 1L;
        long bookingId = 2L;
        when(bookingService.approvingBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).approvingBooking(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение бронирования")
    void getBooking_whenBookingExistsAndUserHasAccess_thenBookingReturned() {
        long userId = 1L;
        long bookingId = 2L;
        when(bookingService.getBooking(userId, bookingId)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService).getBooking(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований пользователя")
    void getUserAllBooking_whenUserExists_thenBookingsReturned() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        when(bookingService.getUserAllBooking(userId, state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).getUserAllBooking(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований пользователя с параметрами по умолчанию")
    void getUserAllBooking_whenParamsDefault_thenBookingsReturned() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        when(bookingService.getUserAllBooking(userId, state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).getUserAllBooking(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований с некорректным параметром from")
    void getUserAllBooking_whenParamFromInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("from", String.valueOf(from))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getUserAllBooking(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований с некорректным параметром size")
    void getUserAllBooking_whenParamSizeMinInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 0;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getUserAllBooking(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований с некорректным параметром size")
    void getUserAllBooking_whenParamSizeMaxInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 999;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getUserAllBooking(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи")
    void getAllBookingByOwner_whenUserExists_thenBookingsReturned() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        when(bookingService.getAllBookingByOwner(userId, state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).getAllBookingByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи с параметрами по умолчанию")
    void getAllBookingByOwner_whenParamsDefault_thenBookingsReturned() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        when(bookingService.getAllBookingByOwner(userId, state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).getAllBookingByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи с некорректным параметром from")
    void getAllBookingByOwner_whenParamFromInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("from", String.valueOf(from))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingByOwner(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи с некорректным параметром size")
    void getAllBookingByOwner_whenParamSizeMinInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 0;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingByOwner(userId, state, from, size);
    }

    @SneakyThrows
    @Test
    @DisplayName("Получение списка всех бронирований владельцем вещи с некорректным параметром size")
    void getAllBookingByOwner_whenParamSizeMaxInvalid_thenException() {
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 999;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingByOwner(userId, state, from, size);
    }
}
*/
