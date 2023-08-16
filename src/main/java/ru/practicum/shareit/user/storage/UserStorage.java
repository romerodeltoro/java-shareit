package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserStorage {

    User addUser(User user);

    User getUser(long id);

    void updateUser(User user);

    Collection<User> getUsers();

    void deleteUser(long id);

}
