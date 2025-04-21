package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    Optional<User> getUser(Long id);

    Collection<User> getAllUsers();

    User updateUser(User user);

    Optional<Boolean> addFriend(Long user1, Long user2);

    Optional<Boolean> removeFriend(Long userId, Long friendId);

    Optional<Collection<User>> getCommonFriends(Long userId, Long friendId);

    Optional<Collection<User>> getFriends(Long userId);
}
