package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.entity.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.rowmapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * Реализация хранилища пользователей в БД.
 * Обеспечивает CRUD-операции для пользователей, и управление дружескими связями.
 *
 * Основные возможности:
 * - Создание, обновление, получение пользователей
 * - Управление дружескими связями (добавление/удаление друзей)
 * - Получение списка друзей и общих друзей
 *
 * Наследует базовую функциональность проверки существования сущностей из BaseDbStorage.
 *
 * Аннотации:
 * @Repository - указывает, что класс является компонентом Spring Data Access Layer
 * @Primary - указывает на предпочтительную реализацию бина
 * @Slf4j - обеспечивает логгирование через SLF4J
 */

@Repository
@Primary
@Slf4j
public class UserDbStorage extends BaseDbStorage implements UserStorage {
    public static final String INSERT_USER_QUERY = """
            INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);
            """;

    private static final String INSERT_FRIENDSHIP_QUERY = """
            INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?);
            """;

    private static final String USER_FRIENDSHIP_FIND_REQUEST = """
            SELECT COUNT(*)
            FROM user_friends
            WHERE user_id = ?
            AND friend_id = ?;
            """;

    public static final String FIND_FRIEND_LIST_OF_USER_BY_USER_ID = """
            SELECT u.*
            FROM users u
            JOIN user_friends uf
            ON u.id = uf.friend_id
            WHERE uf.user_id = ?;
            """;

    public static final String FIND_FRIEND_IDS_LIST_OF_USER_BY_USER_ID = """
            SELECT friend_id
            FROM user_friends
            WHERE user_id = ?;
            """;

    public static final String FIND_COMMON_FRIEND_LIST_BU_USERS_ID = """
            SELECT u.*
            FROM users u
            JOIN user_friends uf1
            ON u.id = uf1.friend_id
            JOIN user_friends uf2
            ON u.id = uf2.friend_id
            WHERE uf1.user_id = ?
            AND uf2.user_id = ?;
            """;

    public static final String FIND_ALL_USERS_QUERY = """
            SELECT * FROM users;
            """;

    public static final String FIND_USER_BY_ID = """
            SELECT *
            FROM users
            WHERE id = ?;
            """;

    public static final String UPDATE_USER_QUERY = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?;
            """;

    private static final String DELETE_FRIENDSHIP_QUERY = """
            DELETE FROM user_friends
            WHERE (user_id = ? AND friend_id = ?);
            """;

    public UserDbStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Создать пользователя -----------------------------------------------------------------------------------------
    @Override
    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, user.getBirthday() != null ? java.sql.Date.valueOf(user.getBirthday()) : null);

            return stmt;
        }, keyHolder);

        Long userId = keyHolder.getKeyAs(Long.class);
        user.setId(userId);
        log.info("Successfully created user with id: {}", userId);

        return user;
    }

    //--- Получить пользователя по id ----------------------------------------------------------------------------------
    @Override
    public Optional<User> getUser(Long id) {
        Objects.requireNonNull(id, "User id cannot be null");

        UserDto userDto = jdbc.queryForObject(FIND_USER_BY_ID, new UserRowMapper(), id);

        if (userDto != null) {
            // загрузить друзей пользователя
            loadFriends(userDto.getId());
            return Optional.of(UserMapper.toUser(userDto));
        }

        log.warn("User with id {} not found", id);
        return Optional.empty();
    }

    //--- Получить список всех пользователей ---------------------------------------------------------------------------
    @Override
    public Collection<User> getAllUsers() {
        List<UserDto> allUsers = jdbc.query(FIND_ALL_USERS_QUERY, new UserRowMapper());

        // загрузить друзей для всех пользователей
        allUsers.forEach(this::enrichUserWithFriends);

        return allUsers.stream()
                .map(UserMapper::toUser)
                .toList();
    }

    //--- Обновить пользователя ----------------------------------------------------------------------------------------
    @Override
    public Optional<User> updateUser(User user) {
        Objects.requireNonNull(user.getId(), "User id cannot be null");

        int rowsAffected = jdbc.update(
                UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsAffected > 0) {
            log.info("Successfully updated user with id: {}", user.getId());
            return Optional.of(user);
        }

        log.warn("No user found with id: {} for update", user.getId());
        return Optional.empty();
    }

    //--- Добавить дружбу ----------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> addFriend(Long user1, Long user2) {
        Objects.requireNonNull(user1, "User1 id cannot be null");
        Objects.requireNonNull(user2, "User2 id cannot be null");

        // проверить существование пользователей
        checkUserExists(user1);
        checkUserExists(user2);

        // проверить существование дружбы
        int count = Optional.ofNullable(jdbc.queryForObject(USER_FRIENDSHIP_FIND_REQUEST, Integer.class, user1, user2))
                .orElse(0);

        if (count > 0) {
            log.warn("Friendship already exists between users {} and {}", user1, user2);
            return Optional.of(false);
        }

        // добавить дружбу
        int rowsAffected = jdbc.update(INSERT_FRIENDSHIP_QUERY, user1, user2);
        log.info("Added friendship between users {} and {}, rows affected: {}", user1, user2, rowsAffected);
        return Optional.of(rowsAffected > 0);
    }

    //--- Удалить дружбу -----------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeFriend(Long userId, Long friendId) {
        // проверить существование пользователей
        checkUserExists(userId);
        checkUserExists(friendId);

        int rowsAffected = jdbc.update(DELETE_FRIENDSHIP_QUERY, userId, friendId);
        return Optional.of(rowsAffected > 0);
    }

    //--- Получить список общих друзей пользователя --------------------------------------------------------------------
    @Override
    public Optional<Collection<User>> getCommonFriends(Long userId, Long friendId) {
        List<UserDto> commonFriends = jdbc.query(
                FIND_COMMON_FRIEND_LIST_BU_USERS_ID, new UserRowMapper(),
                userId, friendId
        );

        // загрузить друзей для выбранных пользователей
        commonFriends.forEach(this::enrichUserWithFriends);

        return Optional.of(commonFriends.stream()
                .map(UserMapper::toUser)
                .toList());
    }

    //--- Получить список друзей пользователя --------------------------------------------------------------------------
    @Override
    public Optional<Collection<User>> getFriends(Long userId) {
        // проверить существование пользователя
        checkUserExists(userId);

        // получить список друзей
        List<UserDto> friends = jdbc.query(FIND_FRIEND_LIST_OF_USER_BY_USER_ID, new UserRowMapper(), userId);

        // загрузить друзей для выбранных пользователей
        friends.forEach(this::enrichUserWithFriends);

        return Optional.of(friends.stream()
                .map(UserMapper::toUser)
                .toList());
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private void checkUserExists(Long userId) {
        checkEntityExists(userId, EntityType.USER);
    }

    private Set<Long> loadFriends(Long userId) {
        return new HashSet<>(jdbc.queryForList(FIND_FRIEND_IDS_LIST_OF_USER_BY_USER_ID, Long.class, userId));
    }

    private void enrichUserWithFriends(UserDto userDto) {
        long userId = userDto.getId();
        userDto.setFriends(loadFriends(userId));
    }
}
