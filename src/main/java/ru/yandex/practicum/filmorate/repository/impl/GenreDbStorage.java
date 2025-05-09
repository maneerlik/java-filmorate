package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.rowmapper.GenreRowMapper;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Реализация хранилища жанров в базе данных.
 * Предоставляет методы для получения информации о жанрах:
 * - поиск жанра по идентификатору
 * - получение списка всех жанров
 *
 * Наследует базовую функциональность проверки существования сущностей из BaseDbStorage.
 *
 * Аннотации:
 * @Repository - указывает, что класс является компонентом Spring Data Access Layer
 */

@Repository
public class GenreDbStorage extends BaseDbStorage implements GenreStorage {
    public static final String FIND_GENRE_BY_ID = """
            SELECT *
            FROM genres
            WHERE id = ?;
            """;

    public static final String FIND_ALL_GENRES = """
            SELECT * FROM genres;
            """;

    public GenreDbStorage(final JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Получить жанр по id ------------------------------------------------------------------------------------------
    @Override
    public Optional<Genre> getGenre(Long id) {
        Objects.requireNonNull(id, "Genre id cannot be null");
        checkGenresExist(id);
        Genre genre = jdbc.queryForObject(FIND_GENRE_BY_ID, new GenreRowMapper(), id);
        return Optional.ofNullable(genre);
    }

    //--- Получить все жанры -------------------------------------------------------------------------------------------
    @Override
    public Collection<Genre> getAllGenres() {
        return jdbc.query(FIND_ALL_GENRES, new GenreRowMapper());
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private void checkGenresExist(Long geneId) {
        checkEntityExists(geneId, EntityType.GENRE);
    }
}
