package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ItemBookerException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CommentRepository commentRepository;


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
        List<CommentDto> comments = getCommentsByItemId(itemId);

        if (item.getUser().getId() != userId) {
            ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
            itemOwnerDto.setComments(comments);
            log.info("Получена вещь '{}'", item);
            return itemOwnerDto;
        }
        List<Booking> lastBookings = bookingRepository.findFirstByItemIdAndEndDateBefore(itemId);
        Booking lastBooking = lastBookings.stream().findFirst().orElse(null);
        List<Booking> nextBookings = bookingRepository.findFirstByItemIdAndStartDateAfter(itemId);
        Booking nextBooking = nextBookings.stream().findFirst().orElse(null);

        ItemOwnerDto itemOwnerDto = ItemMapper.INSTANCE.toItemOwnerDto(item);
        itemOwnerDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
        itemOwnerDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
        itemOwnerDto.setComments(comments);

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
            List<CommentDto> comments = getCommentsByItemId(item.getId());

            itemDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(lastBooking));
            itemDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(nextBooking));
            itemDto.setComments(comments);
            itemDtos.add(itemDto);
        }
        log.info("Получен список вещей пользователя с id '{}'", userId);
        return itemDtos;

    }

    @Override
    public List<ItemDto> searchItems(long userId, String searchText) {
        if (searchText.isEmpty()) {
            log.info("Не было найдено ни одного предмета по запросу '{}'", searchText);
            return Collections.emptyList();
        }
        List<ItemDto> items = itemRepository.findByUserAndNameOrDescription(userId, searchText)
                .stream()
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());

        return items;
    }

    @Override
    public Item ifItemExistReturnItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(
                String.format("Вещи с id %d нет в базе", itemId)));
    }

    @Override
    @Transactional
    public CommentDto postComment(long userId, long itemId, CommentDto commentDto) {
        User user = userService.ifUserExistReturnUser(userId);
        Item item = ifItemExistReturnItem(itemId);
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, userId)
                .stream()
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookings.size() == 0) {
            throw new ItemBookerException(
                    String.format("Вещь с id %d не была арендована пользователем с id %d", itemId, userId));
        }
        Comment comment = commentRepository.save(CommentMapper.INSTANCE.toComment(commentDto));
        comment.setItem(item);
        comment.setAuthor(user);

        return CommentMapper.INSTANCE.toCommentDto(comment);
    }

    private void itemOwnerCheck(long userId, long itemId) {
        if (itemId != userId) {
            throw new ItemOwnerException(
                    String.format("Вещь с id %d не принадлежит пользователю с id %d", itemId, userId));
        }
    }

    private List<CommentDto> getCommentsByItemId(long itemId) {
        return commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper.INSTANCE::toCommentDto)
                .collect(Collectors.toList());
    }
}
