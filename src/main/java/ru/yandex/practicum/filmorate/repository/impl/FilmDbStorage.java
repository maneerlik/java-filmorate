package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.entity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.rowmapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.rowmapper.GenreDtoRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * Реализация хранилища фильмов в БД.
 * Обеспечивает CRUD-операции для фильмов, включая добавление/удаление лайков,
 * получение популярных фильмов и управление жанрами.
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
public class FilmDbStorage extends BaseDbStorage implements FilmStorage {
    private static final String INSERT_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?);
            """;

    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO film_likes (film_id, user_id) VALUES (?, ?);
            """;

    private static final String INSERT_FILM_GENRE_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
            """;

    private static final String FIND_FILM_BY_ID_QUERY = """
            SELECT f.*,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description
            FROM films f
            JOIN mpa_ratings m
            ON f.mpa_rating_id = m.id
            WHERE f.id = ?;
            """;

    private static final String FIND_FILMS_QUERY = """
            SELECT f.*,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description
            FROM films f
            JOIN mpa_ratings m
            ON f.mpa_rating_id = m.id;
            """;

    private static final String FIND_POPULAR_FILMS_QUERY = """
            SELECT f.*,
                   mr.id mpa_id,
                   mr.name mpa_name,
                   mr.description mpa_description,
                   fl.likes_count
            FROM films f
            JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            ORDER BY likes_count DESC,
                     f.id DESC
            LIMIT ?;
            """;

    private static final String FIND_GENRES_ID_BY_FILM_ID_QUERY = """
            SELECT g.id,
                   g.name
            FROM genres g
            JOIN film_genres fg
            ON g.id = fg.genre_id
            WHERE fg.film_id = ?;
            """;

    private static final String FIND_LIKES_BY_FILM_ID_QUERY = """
            SELECT user_id
            FROM film_likes
            WHERE film_id = ?;
            """;

    private static final String UPDATE_FILM_BY_ID = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?;
            """;

    private static final String DELETE_FILM_GENRES_BY_ID = """
            DELETE FROM film_genres WHERE film_id = ?;
            """;

    private static final String DELETE_FILM_LIKES_BY_FILM_AND_USER_ID = """
            DELETE FROM film_likes
            WHERE film_id = ?
            AND user_id = ?;
            """;

    private static final String DELETE_FILM_BY_ID = """
            DELETE FROM films
            WHERE id = ?;
            """;


    public FilmDbStorage(final JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Создание фильма ----------------------------------------------------------------------------------------------
    @Override
    public Film createFilm(Film film) {
        checkMpaRatingExists(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());

            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKeyAs(Long.class);
        film.setId(filmId);

        // Сохранение жанров фильма
        saveFilmGenres(filmId, film.getGenres());

        log.info("Created new film with id: {}", filmId);
        return film;
    }

    //--- Добавление лайка ---------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> addLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }

    //--- Получение фильма по id ---------------------------------------------------------------------------------------
    @Override
    public Optional<Film> getFilm(Long id) {
        checkEntityExists(id, EntityType.FILM);
        FilmDto filmDto = jdbc.queryForObject(FIND_FILM_BY_ID_QUERY, new FilmRowMapper(), id);

        if (filmDto != null) {
            // загрузить жанры и лайки
            enrichFilmWithGenresAndLikes(filmDto);
            return Optional.of(FilmMapper.toFilm(filmDto));
        }

        return Optional.empty();
    }

    //--- Получение списка всех фильмов --------------------------------------------------------------------------------
    @Override
    public Collection<Film> getAllFilms() {
        List<FilmDto> allFilms = jdbc.query(FIND_FILMS_QUERY, new FilmRowMapper());

        // загрузить жанры и лайки для всех фильмов
        allFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return allFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Получение списка популярных фильмов --------------------------------------------------------------------------
    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<FilmDto> popularFilms = jdbc.query(FIND_POPULAR_FILMS_QUERY, new FilmRowMapper(), count);

        // загрузить жанры и лайки для выбранных фильмов
        popularFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return popularFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Обновление фильма --------------------------------------------------------------------------------------------
    @Override
    public Film updateFilm(Film film) {
        checkFilmExists(film);

        jdbc.update(
                UPDATE_FILM_BY_ID,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        // обновить жанры фильма
        jdbc.update(DELETE_FILM_GENRES_BY_ID, film.getId());
        saveFilmGenres(film.getId(), film.getGenres());

        log.info("Updated film with id: {}", film.getId());
        return film;
    }

    //--- Удаление лайка -----------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(DELETE_FILM_LIKES_BY_FILM_AND_USER_ID, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }

    //--- Удаление фильма по id ----------------------------------------------------------------------------------------
    @Override
    public void deleteFilmById(Long filmId) {
        jdbc.update(DELETE_FILM_BY_ID, filmId);
        log.info("Deleted film with id: {}", filmId);
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private void checkGenreExists(Genre genre) {
        checkEntityExists(genre.getId(), EntityType.GENRE);
    }

    private void checkFilmExists(Film film) {
        checkEntityExists(film.getId(), EntityType.FILM);
    }

    private void checkMpaRatingExists(Film film) {
        checkEntityExists(film.getMpa().getId(), EntityType.MPA_RATING);
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            genres.forEach(this::checkGenreExists);
            genres.forEach(genre -> jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId()));
        }
    }

    private Set<GenreDto> loadGenresForFilm(Long filmId) {
        return new HashSet<>(jdbc.query(FIND_GENRES_ID_BY_FILM_ID_QUERY, new GenreDtoRowMapper(), filmId));
    }

    private Set<Long> loadLikesForFilm(Long filmId) {
        return new HashSet<>(jdbc.queryForList(FIND_LIKES_BY_FILM_ID_QUERY, Long.class, filmId));
    }

    private void enrichFilmWithGenresAndLikes(FilmDto filmDto) {
        long filmId = filmDto.getId();
        filmDto.setGenres(loadGenresForFilm(filmId));
        filmDto.setLikes(loadLikesForFilm(filmId));
    }
}
