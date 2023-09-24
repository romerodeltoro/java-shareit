package ru.practicum.shareit.request.service.ipl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    private ItemRequestServiceImpl requestService;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private User requestor;
    private User user;
    private ItemRequestDto requestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        requestor = new User(1L, "Requestor", "requestor@user.com");
        user = new User(2L, "User", "user@user.com");
        item = new Item(1L, "Щетка", "Для обуви", true, user,
                ItemRequestMapper.INSTANCE.toItemRequest(requestDto));

        requestDto = new ItemRequestDto();
        requestDto.setDescription("Хотел бы воспользоваться щёткой для обуви");
    }

    @Test
    @DisplayName("Создание запроса на аренду")
    void createItemRequest_whenUserExists_thenItemRequestCreated() {
        ItemRequest request = ItemRequestMapper.INSTANCE.toItemRequest(requestDto);
        request.setRequestor(requestor);
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        when(requestRepository.save(any())).thenReturn(request);

        ItemRequestDto itemRequest = requestService.createItemRequest(requestor.getId(), requestDto);

        assertEquals(request.getDescription(), itemRequest.getDescription());
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Создание запроса на аренду, когда пользователя не существует")
    void createItemRequest_whenUserDoesNotExist_thenException() {
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> requestService.createItemRequest(requestor.getId(), requestDto));
    }

    @Test
    @DisplayName("Получение списка всех запросов пользователя")
    void getAllUserItemsRequests_whenUserExists_thenItemRequestsReturned() {
        ItemRequest request = ItemRequestMapper.INSTANCE.toItemRequest(requestDto);
        request.setRequestor(requestor);
        request.setId(1L);
        List<ItemRequest> requests = List.of(request);
        List<Item> items = List.of(item);
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        when(itemRepository.findAllByRequestId(request.getId())).thenReturn(items);
        when(requestRepository.getAllByRequestorId(requestor.getId())).thenReturn(requests);

        List<ItemRequestDto> requestDtos = requestService.getAllUserItemsRequests(requestor.getId());

        assertEquals(requests.size(), requestDtos.size());
        assertEquals(requests.get(0).getDescription(), requestDtos.get(0).getDescription());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verify(requestRepository, times(1)).getAllByRequestorId(anyLong());
    }

    @Test
    @DisplayName("Получение списка запросов, когда пользователя не существует")
    void getAllUserItemsRequests_whenUserDoesNotExist_thenException() {
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> requestService.getAllUserItemsRequests(requestor.getId()));
    }

    @Test
    @DisplayName("Получение списка всех запросов")
    void getAllItems_whenUserExists_thenItemRequestsReturned() {
        int from = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(from, size);
        ItemRequest request = ItemRequestMapper.INSTANCE.toItemRequest(requestDto);
        request.setRequestor(requestor);
        request.setId(1L);
        List<ItemRequest> requests = List.of(request);
        List<Item> items = List.of(item);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByRequestId(request.getId())).thenReturn(items);
        when(requestRepository.findAllItems(user.getId(), pageable)).thenReturn(requests);

        List<ItemRequestDto> requestDtos = requestService.getAllItems(user.getId(), from, size);

        assertEquals(requests.size(), requestDtos.size());
        assertEquals(requests.get(0).getDescription(), requestDtos.get(0).getDescription());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verify(requestRepository, times(1)).findAllItems(user.getId(), pageable);
    }

    @Test
    @DisplayName("Получение списка запросов, когда пользователя не существует")
    void getAllItems_whenUserDoesNotExist_thenException() {
        int from = 0;
        int size = 10;
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> requestService.getAllItems(user.getId(), from, size));
    }

    @Test
    @DisplayName("Получение запроса")
    void getItemRequest_whenUserExistsAndRequestIdExists_thenItemRequestReturned() {
        ItemRequest request = ItemRequestMapper.INSTANCE.toItemRequest(requestDto);
        request.setRequestor(requestor);
        request.setId(1L);
        List<Item> items = List.of(item);
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        when(itemRepository.findAllByRequestId(request.getId())).thenReturn(items);
        when(requestRepository.findById(requestor.getId())).thenReturn(Optional.of(request));

        ItemRequestDto itemRequestDto = requestService
                .getItemRequest(requestor.getId(), request.getId());

        assertEquals(request.getDescription(), itemRequestDto.getDescription());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verify(requestRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Получение запроса, когда пользователя не существует")
    void getItemRequest_whenUserDoesNotExist_thenException() {
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> requestService.getItemRequest(requestor.getId(), 1L));
    }

    @Test
    @DisplayName("Получение запроса, когда запроса не существует")
    void getItemRequest_whenRequestIdDoesNotExist_thenException() {
        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));

        assertThrows(ItemNotFoundException.class,
                () -> requestService.getItemRequest(requestor.getId(), 1L));
    }
}
