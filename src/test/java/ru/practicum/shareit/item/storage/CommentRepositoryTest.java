package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Получение списка коментов")
    void findAllByItemId() {
        User user = userRepository.save(new User(1L, "User", "user@user.com"));
        Item item = itemRepository.save(
                new Item(1L, "Item", "Description", true, user, null));
        Comment comment = new Comment(1L, "Text", item, user, LocalDateTime.now());
        repository.save(comment);

        List<Comment> comments = repository.findAllByItemId(comment.getId());

        assertEquals(1, comments.size());
        assertEquals(comment.getText(), comments.get(0).getText());
        assertEquals(comment.getItem(), comments.get(0).getItem());
        assertEquals(comment.getAuthor(), comments.get(0).getAuthor());
        assertEquals(comment.getCreated(), comments.get(0).getCreated());
    }
}