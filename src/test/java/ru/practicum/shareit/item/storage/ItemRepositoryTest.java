package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    private Item item;
    private Item item2;

    @BeforeEach
    void setUp() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);
        item = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto));
        User user = userRepository.save(new User(1L, "Name", "user@user.com"));
        ItemRequest request = new ItemRequest(1L, "description", user, LocalDateTime.now());
        requestRepository.save(request);
        item.setUser(user);
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setName("Отвертка");
        itemDto2.setDescription("Аккумуляторная отвертка");
        itemDto2.setAvailable(true);
        item2 = itemRepository.save(ItemMapper.INSTANCE.toItem(itemDto2));
        item2.setUser(user);
        item2.setRequest(request);
    }

    @Test
    @DisplayName("Получение списка вещей по ID пользователя и сорторовка по возрастанию")
    void findAllByUserIdOrderByIdAsc() {
        List<Item> items = itemRepository
                .findAllByUserIdOrderByIdAsc(1, Pageable.ofSize(2)).getContent();

        assertEquals(2, items.size());
        assertEquals(item, items.get(0));
        assertEquals(item2, items.get(1));
    }

    @Test
    @DisplayName("Получение списка вещей по поисковой строке")
    void findByUserAndNameOrDescription() {
        String searchText = "дРелЬ";
        List<Item> items = itemRepository
                .findByUserAndNameOrDescription(1L, searchText, Pageable.ofSize(2)).getContent();

        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    @DisplayName("Получение списка вещей по ID")
    void findAllByRequestId() {
        List<Item> items = itemRepository.findAllByRequestId(1);

        assertEquals(1, items.size());
        assertEquals(item2, items.get(0));
    }
}