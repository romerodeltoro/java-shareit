package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Autowired
    private final ItemRepository itemRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final ItemMapper itemMapper;

    @Autowired
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        log.info("Создана новая вещь - '{}'", item);

        return ItemMapper.INSTANCE.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = ifItemExistReturnItem(itemId);
        itemOwnerCheck(user.getId(), item.getUser().getId());
        item.setName(itemDto.getName() != null ? itemDto.getName() : item.getName());
        item.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : item.getDescription());
        item.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : item.getAvailable());
        log.info("Вещь '{}' - обновлена", item);

        return ItemMapper.INSTANCE.toItemDto(item);
    }

    @Override
    public ItemDto getItem(long itemId, long userId) {
        Item item = ifItemExistReturnItem(itemId);
        if(item.getUser().getId() != userId) {
            log.info("Получена вещь '{}'", item);
            return ItemMapper.INSTANCE.toItemOwnerDto(item);
        }
        List<Booking> lastBookings = bookingRepository.findFirstByItemIdAndEndDateBefore(itemId);
        Booking lastBooking = lastBookings.stream().findFirst().orElse(null);
        List<Booking> nextBookings = bookingRepository.findFirstByItemIdAndStartDateAfter(itemId);
        Booking nextBooking = nextBookings.stream().findFirst().orElse(null);

        ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
        itemOwnerDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
        itemOwnerDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
        log.info("Владельцем получена вещь '{}'", itemOwnerDto);

        return itemOwnerDto;
    }

    @Override
    public List<ItemDto> getAllUserItems(long userId) {
        userService.ifUserExistReturnUser(userId);

        List<Item> items = itemRepository.findAllByUserIdOrderByIdAsc(userId);
        List<ItemDto> itemDtos = new ArrayList<>();

        for (Item item : items) {
            ItemOwnerDto itemDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
            List<Booking> lastBookings = bookingRepository.findFirstByItemIdAndEndDateBefore(item.getId());
            Booking lastBooking = lastBookings.stream().findFirst().orElse(null);
            List<Booking> nextBookings = bookingRepository.findFirstByItemIdAndStartDateAfter(item.getId());
            Booking nextBooking = nextBookings.stream().findFirst().orElse(null);

            itemDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
            itemDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
            itemDtos.add(itemDto);
        }
        log.info("Получен список вещей пользователя с id '{}'", userId);
        return itemDtos;



        /*return itemRepository.findByUserId(userId).stream()
                .map(ItemMapper.INSTANCE::toItemOwnerDto)
                .collect(Collectors.toList());*/
    }

    @Override
    public List<ItemDto> searchItems(long userId, String searchText) {
        if (searchText.isEmpty()) {
            log.info("Не было найдено ни одного предмета по запросу '{}'", searchText);
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.findByUserAndNameOrDescription(userId, searchText);

        return ItemMapper.INSTANCE.toItemDtoList(items);
    }

    @Override
    public Item ifItemExistReturnItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(
                String.format("Вещи с id %d нет в базе", itemId)));
    }

    private void itemOwnerCheck(long userId, long itemId) {
        if (itemId != userId) {
            throw new ItemOwnerException(
                    String.format("Вещь с id %d не принадлежит пользователю с id %d", itemId, userId));
        }
    }
}
