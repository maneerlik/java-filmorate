package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.EntityType;

import java.util.Optional;

/**
 * Базовый класс для работы с хранилищем данных в БД.
 * Предоставляет общие методы для проверки существования сущностей (пользователей, фильмов,
 * рейтингов MPA и жанров) в базе данных по их идентификаторам.
 * При отсутствии сущности бросает NotFoundException.
 */

@Slf4j
public class BaseDbStorage {
    protected static final String CHECK_EXISTS_USER_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM users
            WHERE id = ?;
            """;

    protected static final String CHECK_EXISTS_FILM_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM films
            WHERE id = ?;
            """;

    protected static final String CHECK_EXISTS_MPA_RATING_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM mpa_ratings
            WHERE id = ?;
            """;

    protected static final String CHECK_EXISTS_GENRE_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM genres
            WHERE id = ?;
            """;

    protected static final String CHECK_EXISTS_DIRECTOR_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM directors
            WHERE id = ?;
            """;

    protected final JdbcTemplate jdbc;

    public BaseDbStorage(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    protected void checkEntityExists(Long id, EntityType type) {
        String query = getQuery(type);

        int count = Optional.ofNullable(jdbc.queryForObject(query, Integer.class, id)).orElse(0);

        if (count == 0) {
            String errorMessage = String.format("%s with id=%d not found", type, id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private String getQuery(EntityType type) {
        return switch (type) {
            case USER -> CHECK_EXISTS_USER_BY_ID_QUERY;
            case FILM -> CHECK_EXISTS_FILM_BY_ID_QUERY;
            case MPA_RATING -> CHECK_EXISTS_MPA_RATING_BY_ID_QUERY;
            case GENRE -> CHECK_EXISTS_GENRE_BY_ID_QUERY;
            case DIRECTOR -> CHECK_EXISTS_DIRECTOR_BY_ID_QUERY;
        };
    }
}
