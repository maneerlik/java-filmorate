package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.entity.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.rowmapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public User createUser(User user) {
        String insertUserQuery = """
                INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, user.getBirthday() != null ? java.sql.Date.valueOf(user.getBirthday()) : null);

            return stmt;
        }, keyHolder);

        Long userId = keyHolder.getKeyAs(Long.class);
        user.setId(userId);

        return user;
    }

    @Override
    public Optional<User> getUser(Long id) {
        String query = """
                SELECT *
                FROM users
                WHERE id = ?;
                """;

        UserDto userDto = jdbc.queryForObject(query, new UserRowMapper(), id);

        if (userDto != null) {
            // загрузить друзей пользователя
            query = """
                    SELECT friend_id
                    FROM user_friends
                    WHERE user_id = ?;
                    """;

            List<Long> friends = jdbc.queryForList(query, Long.class, id);
            userDto.setFriends(new HashSet<>(friends));

            return Optional.of(UserMapper.toUser(userDto));
        }

        return Optional.empty();
    }

    @Override
    public Collection<User> getAllUsers() {
        String findUsersQuery = """
                SELECT *
                FROM users;
                """;

        List<UserDto> userDtos = jdbc.query(findUsersQuery, new UserRowMapper());

        // загрузить друзей для всех пользователей
        for (UserDto userDto : userDtos) {
            String findFriendsQuery = """
                    SELECT friend_id
                    FROM user_friends
                    WHERE user_id = ?;
                    """;

            List<Long> friends = jdbc.queryForList(findFriendsQuery, Long.class, userDto.getId());
            userDto.setFriends(new HashSet<>(friends));
        }

        return userDtos.stream()
                .map(UserMapper::toUser)
                .toList();
    }

    @Override
    public Optional<User> updateUser(User user) {
        String updateUserQuery = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE id = ?;
                """;

        int rowsAffected = jdbc.update(
                updateUserQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        return rowsAffected > 0 ? Optional.of(user) : Optional.empty();
    }

    @Override
    public Optional<Boolean> addFriend(Long user1, Long user2) {
        // проверить существование пользователей
        String checkUserQuery = """
                SELECT COUNT(*)
                FROM users
                WHERE id = ?;
                """;

        int count = jdbc.queryForObject(checkUserQuery, Integer.class, user1);
        if (count == 0) throw new NotFoundException(String.format("User with id=%s not found", user1));

        count = jdbc.queryForObject(checkUserQuery, Integer.class, user2);
        if (count == 0) throw new NotFoundException(String.format("User with id=%s not found", user2));

        // проверить существование дружбы
        String checkFriendshipQuery = """
                SELECT COUNT(*)
                FROM user_friends
                WHERE user_id = ?
                AND friend_id = ?;
                """;

        count = jdbc.queryForObject(checkFriendshipQuery, Integer.class, user1, user2);
        if (count != 0) return Optional.of(false);

        // добавить дружбу
        String insertFriendsQuery = """
                INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)
                """;

        int rowsAffected = jdbc.update(insertFriendsQuery, user1, user2);
        return Optional.of(rowsAffected > 0);
    }

    @Override
    public Optional<Boolean> removeFriend(Long userId, Long friendId) {
        // проверить существование пользователей
        String checkUserQuery = """
                SELECT COUNT(*)
                FROM users
                WHERE id = ?;
                """;

        int count = jdbc.queryForObject(checkUserQuery, Integer.class, userId);
        if (count == 0) throw new NotFoundException(String.format("User with id=%s not found", userId));

        count = jdbc.queryForObject(checkUserQuery, Integer.class, friendId);
        if (count == 0) throw new NotFoundException(String.format("User with id=%s not found", friendId));

        String removeFrindsQuery = """
                DELETE FROM user_friends
                WHERE (user_id = ? AND friend_id = ?);
                """;

        int rowsAffected = jdbc.update(removeFrindsQuery, userId, friendId);
        return Optional.of(rowsAffected > 0);
    }

    @Override
    public Optional<Collection<User>> getCommonFriends(Long userId, Long friendId) {
        String findCommonFriendsQuery = """
                SELECT u.*
                FROM users u
                JOIN user_friends uf1
                ON u.id = uf1.friend_id
                JOIN user_friends uf2
                ON u.id = uf2.friend_id
                WHERE uf1.user_id = ?
                AND uf2.user_id = ?;
                """;

        List<UserDto> userDtos = jdbc.query(findCommonFriendsQuery, new UserRowMapper(), userId, friendId);

        // загрузить друзей для выбранных пользователей
        for (UserDto userDto : userDtos) {
            String findFriendQuery = """
                    SELECT friend_id
                    FROM user_friends
                    WHERE user_id = ?;
                    """;

            List<Long> friends = jdbc.queryForList(findFriendQuery, Long.class, userDto.getId());
            userDto.setFriends(new HashSet<>(friends));
        }

        return Optional.of(userDtos.stream()
                .map(UserMapper::toUser)
                .toList());
    }

    @Override
    public Optional<Collection<User>> getFriends(Long userId) {
        // проверить существование пользователя
        String checkUserQuery = """
                SELECT COUNT(*)
                FROM users
                WHERE id = ?;
                """;

        int count = jdbc.queryForObject(checkUserQuery, Integer.class, userId);
        if (count == 0) throw new NotFoundException(String.format("User with id=%s not found", userId));

        String findFriendsQuery = """
                SELECT u.*
                FROM users u
                JOIN user_friends uf
                ON u.id = uf.friend_id
                WHERE uf.user_id = ?;
                """;

        List<UserDto> userDtos = jdbc.query(findFriendsQuery, new UserRowMapper(), userId);

        // загрузить друзей для выбранных пользователей
        for (UserDto userDto : userDtos) {
            String findFriendQuery = """
                    SELECT friend_id
                    FROM user_friends
                    WHERE user_id = ?;
                    """;

            List<Long> friends = jdbc.queryForList(findFriendQuery, Long.class, userDto.getId());
            userDto.setFriends(new HashSet<>(friends));
        }

        return Optional.of(userDtos.stream()
                .map(UserMapper::toUser)
                .toList());
    }
}
