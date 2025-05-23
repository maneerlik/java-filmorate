/*
package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.*;

import static java.util.Objects.nonNull;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1L;


    @Override
    public User createUser(User user) {
        user.setId(id);
        users.put(id++, user);
        return user;
    }

    @Override
    public Optional<User> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<Boolean> addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new IllegalArgumentException("Cannot add yourself as friend");
        return Optional.of(updateFriendship(userId, friendId, true));
    }

    @Override
    public Optional<Boolean> removeFriend(Long userId, Long friendId) {
        return Optional.of(updateFriendship(userId, friendId, false));
    }

    @Override
    public Optional<Collection<User>> getCommonFriends(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        validateUsersExist(user, friend, userId, friendId);

        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(friend.getFriends());

        List<User> commonFriends = commonIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();

        return Optional.of(commonFriends);
    }

    @Override
    public Optional<Collection<User>> getFriends(Long userId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException(String.format("User with id=%s not found", userId));

        List<User> friends = user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();

        return Optional.of(friends);
    }

    @Override
    public Optional<User> updateUser(User user) {
        long userId = user.getId();
        User existUser = users.get(userId);

        if (existUser == null) throw new NotFoundException(String.format("User with id=%s not found", userId));

        User updatedUser = new User();

        updatedUser.setId(userId);
        updatedUser.setEmail(getOrDefault(user.getEmail(), existUser.getEmail()));
        updatedUser.setLogin(getOrDefault(user.getLogin(), existUser.getLogin()));
        updatedUser.setName(user.getName() == null || user.getName().equals(user.getLogin())
                ? existUser.getName()
                : user.getName());
        updatedUser.setBirthday(getOrDefault(user.getBirthday(), existUser.getBirthday()));

        users.put(userId, updatedUser);
        return Optional.of(updatedUser);
    }

    private boolean updateFriendship(Long userId, Long friendId, boolean add) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        validateUsersExist(user, friend, userId, friendId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();

        if (add) {
            if (userFriends.contains(friendId)) throw new ConflictException("Friendship already exists");
            userFriends.add(friendId);
            friendFriends.add(userId);
        } else {
            if (!userFriends.contains(friendId)) return false;
            userFriends.remove(friendId);
            friendFriends.remove(userId);
        }

        return true;
    }

    private void validateUsersExist(User user, User friend, Long userId, Long friendId) {
        if (user == null) {
            throw new NotFoundException(String.format("User with id=%s not found", userId));
        }
        if (friend == null) {
            throw new NotFoundException(String.format("User with id=%s not found", friendId));
        }
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return nonNull(value) ? value : defaultValue;
    }
}
*/
