package ru.practicum.shareit.request.service.ipl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestServiceImpl requestService;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private UserDto requestorDto;
    private UserDto userDto;
    private ItemRequestDto requestDto;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        requestorDto = new UserDto();
        requestorDto.setName("Requestor");
        requestorDto.setEmail("requestor@user.com");
        userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@user.com");
        itemDto = new ItemDto();
        itemDto.setName("Щетка");
        itemDto.setDescription("Для обуви");
        itemDto.setAvailable(true);

        requestDto = new ItemRequestDto();
        requestDto.setDescription("Хотел бы воспользоваться щёткой для обуви");
    }

    @Test
    @DisplayName("Создание запроса на аренду")
    void createItemRequest_whenUserExists_thenItemRequestCreated() {
        User requestor = userRepository.save(UserMapper.INSTANCE.toUser(requestorDto));

        ItemRequestDto actualRequestDto = requestService.createItemRequest(requestor.getId(), requestDto);

        assertNotNull(actualRequestDto.getId());
        assertEquals(requestDto.getDescription(), actualRequestDto.getDescription());
    }

    @Test
    @DisplayName("Создание запроса на аренду, когда пользователя не существует")
    void createItemRequest_whenUserDoesNotExist_thenException() {

        assertThrows(UserNotFoundException.class,
                () -> requestService.createItemRequest(1L, requestDto));
    }

    @Test
    @DisplayName("Получение списка всех запросов пользователя")
    void getAllUserItemsRequests_whenUserExists_thenItemRequestsReturned() {
        User requestor = userRepository.save(UserMapper.INSTANCE.toUser(requestorDto));
        ItemRequest request = requestRepository.save(ItemRequestMapper.INSTANCE.toItemRequest(requestDto));
        request.setRequestor(requestor);
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setRequestId(request.getId());
        List<ItemRequest> requests = List.of(request);

        List<ItemRequestDto> actualList = requestService.getAllUserItemsRequests(requestor.getId());

        assertEquals(requests.size(), actualList.size());
        assertEquals(requests.get(0).getDescription(), actualList.get(0).getDescription());
    }

    @Test
    @DisplayName("Получение списка запросов, когда пользователя не существует")
    void getAllUserItemsRequests_whenUserDoesNotExist_thenException() {

        assertThrows(UserNotFoundException.class,
                () -> requestService.getAllUserItemsRequests(1L));
    }

    @Test
    @DisplayName("Получение списка всех запросов")
    void getAllItems_whenUserExists_thenItemRequestsReturned() {
        int from = 0;
        int size = 10;
        User user = userRepository.save(UserMapper.INSTANCE.toUser(userDto));
        User requestor = userRepository.save(UserMapper.INSTANCE.toUser(requestorDto));
        ItemRequest request = requestRepository.save(ItemRequestMapper.INSTANCE.toItemRequest(requestDto));
        request.setRequestor(requestor);
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setRequestId(request.getId());
        List<ItemRequest> requests = List.of(request);

        List<ItemRequestDto> actualList = requestService.getAllItems(user.getId(), from, size);

        assertEquals(requests.size(), actualList.size());
        assertEquals(requests.get(0).getDescription(), actualList.get(0).getDescription());
    }

    @Test
    @DisplayName("Получение списка запросов, когда пользователя не существует")
    void getAllItems_whenUserDoesNotExist_thenException() {
        int from = 0;
        int size = 10;

        assertThrows(UserNotFoundException.class,
                () -> requestService.getAllItems(1L, from, size));
    }

    @Test
    @DisplayName("Получение запроса")
    void getItemRequest_whenUserExistsAndRequestIdExists_thenItemRequestReturned() {
        User requestor = userRepository.save(UserMapper.INSTANCE.toUser(requestorDto));
        ItemRequest request = requestRepository.save(ItemRequestMapper.INSTANCE.toItemRequest(requestDto));
        request.setRequestor(requestor);
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setRequestId(request.getId());

        ItemRequestDto actualRequestDto = requestService
                .getItemRequest(requestor.getId(), request.getId());

        assertEquals(request.getDescription(), actualRequestDto.getDescription());
    }

    @Test
    @DisplayName("Получение запроса, когда пользователя не существует")
    void getItemRequest_whenUserDoesNotExist_thenException() {

        assertThrows(UserNotFoundException.class,
                () -> requestService.getItemRequest(1L, 1L));
    }

    @Test
    @DisplayName("Получение запроса, когда запроса не существует")
    void getItemRequest_whenRequestIdDoesNotExist_thenException() {
        User requestor = userRepository.save(UserMapper.INSTANCE.toUser(requestorDto));

        assertThrows(ItemNotFoundException.class,
                () -> requestService.getItemRequest(requestor.getId(), 1L));
    }
}