package ru.practicum.shareit.request.service.ipl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = ifUserExistReturnUser(userId);
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.INSTANCE.toItemRequest(itemRequestDto));
        itemRequest.setRequestor(user);
        log.info("Создан новый запрос - '{}'", itemRequest);

        return ItemRequestMapper.INSTANCE.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllUserItemsRequests(long userId) {

        ifUserExistReturnUser(userId);
        List<ItemRequestDto> itemRequests = itemRequestRepository.getAllByRequestorId(userId).stream()
                .map(ItemRequestMapper.INSTANCE::toItemRequestDto)
                .peek(itemRequestDto -> {
                    List<Item> items = itemRepository.findAllByRequestId(itemRequestDto.getId());
                    itemRequestDto.setItems(items.stream()
                            .map(ItemMapper.INSTANCE::toItemDto).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
        log.info("Получен список запросов пользователя с id '{}'", userId);

        return itemRequests;
    }

    @Override
    public List<ItemRequestDto> getAllItems(long userId, Integer from, Integer size) {
        ifUserExistReturnUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findAllItems(userId, pageable);

        List<ItemRequestDto> itemRequests = requests.stream()
                .map(ItemRequestMapper.INSTANCE::toItemRequestDto)
                .peek(itemRequestDto -> {
                    List<Item> items = itemRepository.findAllByRequestId(itemRequestDto.getId());
                    itemRequestDto.setItems(items.stream()
                            .map(ItemMapper.INSTANCE::toItemDto).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());

        log.info("Получен список запросов");

        return itemRequests;

    }

    @Override
    public ItemRequestDto getItemRequest(Long userId, Long requestId) {
        ifUserExistReturnUser(userId);
        ItemRequestDto itemRequestDto =
                ItemRequestMapper.INSTANCE.toItemRequestDto(itemRequestRepository.findById(requestId)
                        .orElseThrow(() -> new ItemNotFoundException(
                                String.format("Запроса с id %d нет в базе", requestId))));
        List<Item> items = itemRepository.findAllByRequestId(itemRequestDto.getId());
        itemRequestDto.setItems(items.stream()
                .map(ItemMapper.INSTANCE::toItemDto).collect(Collectors.toList()));
        log.info("Получен запрос с id '{}'", requestId);

        return itemRequestDto;
    }

    private User ifUserExistReturnUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("Пользователя с id %d нет в базе", userId)));
    }
}
