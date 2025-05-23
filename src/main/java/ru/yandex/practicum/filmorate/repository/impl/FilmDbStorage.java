package ru.yandex.practicum.filmorate.repository.impl;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.entity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enumeration.SearchParameter;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.rowmapper.DirectorDtoRowMapper;
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
 * @Slf4j - обеспечивает логирование через SLF4J
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

    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            INSERT INTO film_directors (film_id, director_id) VALUES (?, ?);
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

    private static final String FIND_DIRECTORS_ID_BY_FILM_ID_QUERY = """
            SELECT d.id,
                   d.name
            FROM directors d
            JOIN film_directors fd
            ON d.id = fd.director_id
            WHERE fd.film_id = ?;
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

    private static final String DELETE_FILM_DIRECTORS_BY_ID = """
            DELETE FROM film_directors WHERE film_id = ?;
            """;

    private static final String DELETE_FILM_LIKES_BY_FILM_AND_USER_ID = """
            DELETE FROM film_likes
            WHERE film_id = ?
            AND user_id = ?;
            """;

    private static final String FIND_FILMS_DIRECTORS_FOR_DATES = """
            SELECT f.*,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description,
                   fl.likes_count
            FROM films f
            JOIN mpa_ratings m
            ON f.mpa_rating_id = m.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id
                FROM film_directors
                WHERE director_id = ?
            )
            ORDER BY f.release_date ASC;
            """;

    private static final String FIND_FILMS_DIRECTORS_FOR_LIKES = """
            SELECT f.*,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description,
                   fl.likes_count
            FROM films f
            JOIN mpa_ratings m
            ON f.mpa_rating_id = m.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id
                FROM film_directors
                WHERE director_id = ?
            )
            ORDER BY fl.likes_count DESC;
            """;

    private static final String BASE_SEARCH_FILMS_BY_CONDITIONS = """
            SELECT f.*,
                   mr.id mpa_id,
                   mr.name mpa_name,
                   mr.description mpa_description
            FROM films f
            JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id 
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE 
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
        // Сохранение режиссеров
        saveFilmDirectors(filmId, film.getDirectors());

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
        FilmDto filmDto = jdbc.queryForObject(FIND_FILM_BY_ID_QUERY, new FilmRowMapper(), id);

        if (filmDto != null) {
            // загрузить жанры, режиссеров и лайки
            enrichFilmWithGenresAndLikes(filmDto);
            return Optional.of(FilmMapper.toFilm(filmDto));
        }

        return Optional.empty();
    }

    //--- Получение списка всех фильмов --------------------------------------------------------------------------------
    @Override
    public Collection<Film> getAllFilms() {
        List<FilmDto> allFilms = jdbc.query(FIND_FILMS_QUERY, new FilmRowMapper());

        // загрузить жанры, режиссеров и лайки для всех фильмов
        allFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return allFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Получение списка популярных фильмов --------------------------------------------------------------------------
    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<FilmDto> popularFilms = jdbc.query(FIND_POPULAR_FILMS_QUERY, new FilmRowMapper(), count);

        // загрузить жанры, режиссеров и лайки для выбранных фильмов
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

        // обновить режиссеров фильма
        jdbc.update(DELETE_FILM_DIRECTORS_BY_ID, film.getId());
        saveFilmDirectors(film.getId(), film.getDirectors());

        log.info("Updated film with id: {}", film.getId());
        return film;
    }

    //--- Удаление лайка -----------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(DELETE_FILM_LIKES_BY_FILM_AND_USER_ID, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }

    //--- Получение фильмов режиссера, отсортированных по годам или лайкам ---------------------------------------------
    public Collection<Film> getFilmsDirector(Long directorId, String sortBy) {
        List<FilmDto> films = new ArrayList<>();
        if (sortBy.equals("likes")) {
            films = jdbc.query(FIND_FILMS_DIRECTORS_FOR_LIKES, new FilmRowMapper(), directorId);
        } else if (sortBy.equals("year")) {
            films = jdbc.query(FIND_FILMS_DIRECTORS_FOR_DATES, new FilmRowMapper(), directorId);
        } else {
            throw new IllegalArgumentException("sortBy must be 'likes' or 'year'");
        }

        // загрузить жанры, режиссеров и лайки для всех фильмов
        films.forEach(this::enrichFilmWithGenresAndLikes);

        return films.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Поиск фильмов ------------------------------------------------------------------------------------------------
    @Override
    public Collection<Film> searchFilms(String query, List<SearchParameter> searchParameters) {
        String separator = "OR ";
        String parameterSetting = " LIKE '%" + query.toLowerCase() + "%' ";

        List<String> stringSearchParameters = searchParameters.stream()
                .map(searchParameter -> "LOWER(" + searchParameter.getSqlField() + ")" + parameterSetting)
                .toList();
        String queryCondition = Joiner.on(separator).join(stringSearchParameters) + """
                ORDER BY likes_count,
                f.id;
                """;

        List<FilmDto> foundFilms = jdbc.query(BASE_SEARCH_FILMS_BY_CONDITIONS + queryCondition,
                new FilmRowMapper());

        // загрузить жанры, лайки и режиссеров для найденных фильмов
        foundFilms.forEach(this::enrichFilmWithGenresLikesAndDirectors);

        return foundFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private void checkGenreExists(Genre genre) {
        checkEntityExists(genre.getId(), EntityType.GENRE);
    }

    private void checkDirectorExists(Director director) {
        checkEntityExists(director.getId(), EntityType.DIRECTOR);
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

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors != null && !directors.isEmpty()) {
            directors.forEach(this::checkDirectorExists);
            directors.forEach(director -> jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, director.getId()));
        }
    }

    private Set<GenreDto> loadGenresForFilm(Long filmId) {
        return new HashSet<>(jdbc.query(FIND_GENRES_ID_BY_FILM_ID_QUERY, new GenreDtoRowMapper(), filmId));
    }

    private Set<DirectorDto> loadDirectorsForFilm(Long filmId) {
        return new HashSet<>(jdbc.query(FIND_DIRECTORS_ID_BY_FILM_ID_QUERY, new DirectorDtoRowMapper(), filmId));
    }

    private Set<Long> loadLikesForFilm(Long filmId) {
        return new HashSet<>(jdbc.queryForList(FIND_LIKES_BY_FILM_ID_QUERY, Long.class, filmId));
    }

    private void enrichFilmWithGenresAndLikes(FilmDto filmDto) {
        long filmId = filmDto.getId();
        filmDto.setGenres(loadGenresForFilm(filmId));
        filmDto.setDirectors(loadDirectorsForFilm(filmId));
        filmDto.setLikes(loadLikesForFilm(filmId));
    }
}
