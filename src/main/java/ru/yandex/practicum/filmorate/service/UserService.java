package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.FriendshipException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@Validated
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public User create(@Valid @RequestBody User user) {
        User createdUser = userStorage.createUser(user);
        log.info("User created: {}", createdUser);
        return createdUser;
    }

    public User getUser(Long userId) {
        return userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    public User update(@Validated(UpdateValidationGroup.class) @RequestBody User user) {
        Optional<User> updatedUser = userStorage.updateUser(user);
        if (updatedUser.isEmpty())
            throw new NotFoundException(String.format("User with id=%s not found", user.getId()));
        log.info("User updated: {}", updatedUser);
        return updatedUser.get();
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId)
                .orElseThrow(() -> new FriendshipException("Failed to add friend"));
        log.info("Friendship created between {} and {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId)
                .orElseThrow(() -> new FriendshipException("Friendship not found"));
        log.info("Friendship removed between {} and {}", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        return userStorage.getCommonFriends(userId, friendId)
                .orElseThrow(() -> new FriendshipException("Common friends not found"));
    }

    public Collection<User> getFriends(Long userId) {
        return userStorage.getFriends(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}
