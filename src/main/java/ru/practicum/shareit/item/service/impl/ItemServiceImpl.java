package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ItemBookerException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemOwnerException;
import ru.practicum.shareit.exception.UserNotFoundException;
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
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemRepository itemRepository;


    private final UserRepository userRepository;


    private final BookingRepository bookingRepository;


    private final CommentRepository commentRepository;


    @Transactional
    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User user = ifUserExistReturnUser(userId);
        Item item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        item.setUser(user);
        log.info("Создана новая вещь - '{}'", item);

        return ItemMapper.INSTANCE.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        User user = ifUserExistReturnUser(userId);
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
    public List<ItemDto> getAllUserItems(long userId, Integer from, Integer size) {
        ifUserExistReturnUser(userId);
        Pageable pageable = PageRequest.of(from, size);

        List<Item> items = itemRepository.findAllByUserIdOrderByIdAsc(userId, pageable).getContent();
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper.INSTANCE::toItemOwnerDto)
                .peek(itemDto -> {
                    List<Booking> lastBookings = bookingRepository.findFirstByItemIdAndEndDateBefore(itemDto.getId());
                    itemDto.setLastBooking(BookingMapper.INSTANCE.lastBookingDto(
                            lastBookings.stream().findFirst().orElse(null))
                    );
                    List<Booking> nextBookings = bookingRepository.findFirstByItemIdAndStartDateAfter(itemDto.getId());
                    itemDto.setNextBooking(BookingMapper.INSTANCE.nextBookingDto(
                            nextBookings.stream().findFirst().orElse(null)));

                    List<CommentDto> comments = getCommentsByItemId(itemDto.getId());
                    itemDto.setComments(comments);
                })
                .collect(Collectors.toList());
        log.info("Получен список вещей пользователя с id '{}'", userId);
        return itemDtos;

    }

    @Override
    public List<ItemDto> searchItems(long userId, String searchText, Integer from, Integer size) {
        if (searchText.isEmpty()) {
            log.info("Не было найдено ни одного предмета по запросу '{}'", searchText);
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(from, size);

        List<ItemDto> items = itemRepository.findByUserAndNameOrDescription(userId, searchText, pageable)
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
        User user = ifUserExistReturnUser(userId);
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

    private User ifUserExistReturnUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("Пользователя с id %d нет в базе", userId)));
    }
}
