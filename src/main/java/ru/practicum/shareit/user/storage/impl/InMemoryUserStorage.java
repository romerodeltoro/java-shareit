package ru.practicum.shareit.user.storage.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;

@Component
public class InMemoryUserStorage implements UserStorage {

    private HashMap<Long, User> users = new HashMap<>();

    private static int incrementedUserId = 0;

    private long setIncrementedUserId() {
        return ++incrementedUserId;
    }

    @Override
    public User addUser(User user) {
        user.setId(setIncrementedUserId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(long id) {
        return users.get(id);
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public void deleteUser(long id) {
        users.remove(id);
    }
}
