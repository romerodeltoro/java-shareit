package ru.practicum.shareit.user.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Получение пользователя по email")
    void findByEmail() {
        User user = new User(1L, "User", "user@user.com");
        userRepository.save(user);

        Optional<User> actualUser = userRepository.findByEmail(user.getEmail());

        assertTrue(actualUser.isPresent());
        assertEquals("user@user.com", actualUser.get().getEmail());
    }
}
