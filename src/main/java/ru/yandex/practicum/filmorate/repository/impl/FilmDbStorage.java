package ru.yandex.practicum.filmorate.repository.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.SqlParameterException;
import ru.yandex.practicum.filmorate.mapper.entity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enumeration.SearchParameter;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.rowmapper.DirectorDtoRowMapper;
import ru.yandex.practicum.filmorate.rowmapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.rowmapper.GenreDtoRowMapper;

/**
 * Реализация хранилища фильмов в БД. Обеспечивает CRUD-операции для фильмов, включая
 * добавление/удаление лайков, получение популярных фильмов и управление жанрами.
 * <p>
 * Наследует базовую функциональность проверки существования сущностей из BaseDbStorage.
 * <p>
 * Аннотации:
 *
 * @Repository - указывает, что класс является компонентом Spring Data Access Layer
 * @Slf4j - обеспечивает логгирование через SLF4J
 */

@Repository
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

    private static final String FIND_COMMON_FILMS_QUERY = """
            SELECT f.*,
                   mr.id mpa_id,
                   mr.name mpa_name,
                   mr.description mpa_description
            FROM films f
            JOIN (
                SELECT f.id
                FROM films f
                JOIN film_likes fl ON f.id = fl.film_id
                WHERE fl.user_id IN (?, ?)
                GROUP BY f.id
                HAVING COUNT(DISTINCT fl.user_id) = 2
            ) cf ON f.id = cf.id
            JOIN film_likes fl ON cf.id = fl.film_id
            JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
            GROUP BY f.id
            ORDER BY COUNT(fl.user_id) DESC;
            """;

    private static final String FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR_QUERY = """
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
            JOIN (
                SELECT film_id
                FROM film_genres
                WHERE genre_id = ?
            ) fg ON f.id = fg.film_id
            WHERE YEAR(f.release_date) = ?;
            """;

    private static final String FIND_POPULAR_FILMS_BY_GENRE_QUERY = """
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
            JOIN (
                SELECT film_id
                FROM film_genres
                WHERE genre_id = ?
            ) fg ON f.id = fg.film_id;
            """;

    private static final String FIND_POPULAR_FILMS_BY_YEAR_QUERY = """
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
            WHERE YEAR(f.release_date) = ?;
            """;

    private static final String FIND_POPULAR_LIMIT = """
            LIMIT ?;
            """;

    private static final String FIND_POPULAR_NOT_LIMIT = """
            ;
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

    private static final String DELETE_FILM_BY_ID = """
            DELETE FROM films
            WHERE id = ?;
            """;

    private static final String SORT_FOR_SEARCH_QUERY = """
            ORDER BY likes_count DESC,
            f.id;
            """;

    private static final String SEARCH_BY_TITLE = """
            UPPER(f.name) LIKE ?
            """;

    private static final String SEARCH_BY_DIRECTOR = """
            UPPER(d.name) LIKE ?
            """;

    private static final String FIND_LIST_OF_USER_ID_FOR_RECOMMENDATION = """
            SELECT f.*, mr.id mpa_id, mr.name mpa_name, mr.description mpa_description
            FROM films f
            JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
            WHERE f.id IN (
                SELECT film_id
                FROM film_likes
                WHERE user_id IN (
                    SELECT u_cl.user_id
                    FROM (
                        (SELECT user_id, COUNT(*) common_likes
                        FROM film_likes
                        WHERE film_id IN (
                            SELECT film_id
                            FROM film_likes
                            WHERE user_id = ?)
                        GROUP BY user_id) u_cl
                    RIGHT JOIN (
                        SELECT user_id, COUNT(*) all_likes
                        FROM film_likes
                        GROUP BY user_id) u_al ON u_al.user_id = u_cl.user_id)
                    WHERE (all_likes - common_likes > 0))
            AND f.id NOT IN (
                SELECT film_id
                FROM film_likes
                WHERE user_id = ?));
            """;

    private static final String FIND_LIST_OF_FILM_ID_BY_USER_ID = """
            SELECT film_id
            FROM film_likes
            WHERE user_id = ?;
            """;

    private static final int COUNT_IS_ZERO = 0;
    private static final int ID_GENRE_IS_ZERO = 0;
    private static final int YEAR_IS_ZERO = 0;
    private static final int YEAR_IS_1895 = 1895;

    public FilmDbStorage(final JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Создание фильма ----------------------------------------------------------------------------------------------
    @Override
    public Film createFilm(Film film) {
        checkMpaRatingExists(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_FILM_QUERY,
                    Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());

            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKeyAs(Long.class);
        film.setId(filmId);

        // обновить жанры фильма
        jdbc.update(DELETE_FILM_GENRES_BY_ID, film.getId());
        film.setGenres(saveFilmGenres(film.getId(), film.getGenres()));

        // обновить режиссеров фильма
        jdbc.update(DELETE_FILM_DIRECTORS_BY_ID, film.getId());
        film.setDirectors(saveFilmDirectors(film.getId(), film.getDirectors()));

        log.info("Created new film with id: {}", filmId);
        return film;
    }

    //--- Добавление лайка ---------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> addLike(Long filmId, Long userId) {
        removeLike(filmId, userId);
        int rowsAffected = jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }

    //--- Получение фильма по id ---------------------------------------------------------------------------------------
    @Override
    public Optional<Film> getFilm(Long id) {
        checkFilmExists(id);
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

    //--- Получение списка популярных фильмов по жанру и году ---------------------------------------------------------
    @Override
    public Collection<Film> getPopularFilmsByGenreAndYear(int count, Long genreId, int year) {
        List<FilmDto> popularFilms = new ArrayList<>();
        if (count != COUNT_IS_ZERO) {
            if (genreId != ID_GENRE_IS_ZERO && year >= YEAR_IS_1895) {
                checkGenresExist(genreId);
                popularFilms = jdbc.query(FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR_QUERY + FIND_POPULAR_LIMIT,
                        new FilmRowMapper(), genreId, year, count);
            } else if (genreId != ID_GENRE_IS_ZERO && year == YEAR_IS_ZERO) {
                checkGenresExist(genreId);
                popularFilms = jdbc.query(FIND_POPULAR_FILMS_BY_GENRE_QUERY + FIND_POPULAR_LIMIT,
                        new FilmRowMapper(), genreId, count);
            } else if (year >= YEAR_IS_1895) {
                popularFilms = jdbc.query(FIND_POPULAR_FILMS_BY_YEAR_QUERY + FIND_POPULAR_LIMIT,
                        new FilmRowMapper(), year, count);
            } else {
                throw new IllegalArgumentException("Release date film is incorrect");
            }
        } else {
            if (genreId != ID_GENRE_IS_ZERO && year >= YEAR_IS_1895) {
                checkGenresExist(genreId);
                popularFilms = jdbc.query(
                        FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR_QUERY + FIND_POPULAR_NOT_LIMIT,
                        new FilmRowMapper(), genreId, year);
            } else if (genreId != ID_GENRE_IS_ZERO && year == YEAR_IS_ZERO) {
                checkGenresExist(genreId);
                popularFilms = jdbc.query(FIND_POPULAR_FILMS_BY_GENRE_QUERY + FIND_POPULAR_NOT_LIMIT,
                        new FilmRowMapper(), genreId);
            } else if (year >= YEAR_IS_1895) {
                popularFilms = jdbc.query(FIND_POPULAR_FILMS_BY_YEAR_QUERY + FIND_POPULAR_NOT_LIMIT,
                        new FilmRowMapper(), year);
            } else {
                throw new IllegalArgumentException("Release date film is incorrect");
            }
        }

        // загрузить жанры, режиссеров и лайки для выбранных фильмов
        popularFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return popularFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Получение списка общих фильмов -------------------------------------------------------------------------------
    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        List<FilmDto> commonFilms = jdbc.query(FIND_COMMON_FILMS_QUERY, new FilmRowMapper(), userId,
                friendId);

        // загрузить жанры, режиссеров и лайки для выбранных фильмов
        commonFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return commonFilms.stream()
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
        film.setGenres(saveFilmGenres(film.getId(), film.getGenres()));

        // обновить режиссеров фильма
        jdbc.update(DELETE_FILM_DIRECTORS_BY_ID, film.getId());
        film.setDirectors(saveFilmDirectors(film.getId(), film.getDirectors()));

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
    @Override
    public Collection<Film> getFilmsDirector(Long directorId, String sortBy) {
        checkDirectorExists(directorId);

        List<FilmDto> films;

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
    public Collection<Film> searchFilms(String query, List<String> searchParameters) {
        List<FilmDto> foundFilms = runQueryForSearchFilms(query.toUpperCase(), searchParameters);

        // загрузить жанры, лайки и режиссеров для найденных фильмов
        foundFilms.forEach(this::enrichFilmWithGenresAndLikes);

        return foundFilms.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    //--- Удаление фильма по id ----------------------------------------------------------------------------------------
    @Override
    public void deleteFilmById(Long filmId) {
        jdbc.update(DELETE_FILM_BY_ID, filmId);
        log.info("Deleted film with id: {}", filmId);
    }

    //--- Фильмы для рекомендации --------------------------------------------------------------------------------------
    @Override
    public Collection<Film> getRecommendations(Long userId) {

        // Найти лайки пользователя
        final List<Long> usersLikes = jdbc.queryForList(FIND_LIST_OF_FILM_ID_BY_USER_ID, Long.class,
                userId);
        if (usersLikes.isEmpty()) {
            return List.of();
        }

        List<FilmDto> recommendation = jdbc.query(connection -> {
            PreparedStatement stmt = connection.prepareStatement(FIND_LIST_OF_USER_ID_FOR_RECOMMENDATION);

            stmt.setLong(1, userId);
            stmt.setLong(2, userId);

            return stmt;
        }, new FilmRowMapper());

        // загрузить жанры, лайки и режиссеров для найденных фильмов
        recommendation.forEach(this::enrichFilmWithGenresAndLikes);

        return recommendation.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private List<Genre> saveFilmGenres(Long filmId, List<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            genres.forEach(this::checkGenreExists);
            if (genres.size() != new HashSet<>(genres).size()) {
                List<Genre> genresNew = new ArrayList<>();
                for (Genre genre : genres) {
                    if (!genresNew.contains(genre)) {
                        genresNew.add(genre);
                    }
                }
                genres = genresNew;
            }
            genres.forEach(genre -> jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId()));
        }
        return genres;
    }

    private List<Director> saveFilmDirectors(Long filmId, List<Director> directors) {
        if (directors != null && !directors.isEmpty()) {
            directors.forEach(this::checkDirectorExists);
            if (directors.size() != new HashSet<>(directors).size()) {
                List<Director> directorsNew = new ArrayList<>();
                for (Director director : directors) {
                    if (!directorsNew.contains(director)) {
                        directorsNew.add(director);
                    }
                }
                directors = directorsNew;
            }
            directors.forEach(director -> jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, director.getId()));
        }
        return directors;
    }

    private Set<GenreDto> loadGenresForFilm(Long filmId) {
        return new HashSet<>(
                jdbc.query(FIND_GENRES_ID_BY_FILM_ID_QUERY, new GenreDtoRowMapper(), filmId));
    }

    private Set<DirectorDto> loadDirectorsForFilm(Long filmId) {
        return new HashSet<>(
                jdbc.query(FIND_DIRECTORS_ID_BY_FILM_ID_QUERY, new DirectorDtoRowMapper(), filmId));
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

    private List<FilmDto> runQueryForSearchFilms(String query, List<String> searchParameters) {
        final String SQLQuery;

        if (searchParameters.contains(SearchParameter.DIRECTOR.name()) && searchParameters.contains(
                SearchParameter.TITLE.name())) {
            SQLQuery = BASE_SEARCH_FILMS_BY_CONDITIONS + SEARCH_BY_TITLE + " OR " + SEARCH_BY_DIRECTOR
                    + SORT_FOR_SEARCH_QUERY;
            return jdbc.query(connection -> {
                PreparedStatement stmt = connection.prepareStatement(SQLQuery);

                stmt.setString(1, "%" + query + "%");
                stmt.setString(2, "%" + query + "%");

                return stmt;
            }, new FilmRowMapper());
        } else if (searchParameters.contains(SearchParameter.DIRECTOR.name())) {
            SQLQuery = BASE_SEARCH_FILMS_BY_CONDITIONS + SEARCH_BY_DIRECTOR + SORT_FOR_SEARCH_QUERY;
        } else if (searchParameters.contains(SearchParameter.TITLE.name())) {
            SQLQuery = BASE_SEARCH_FILMS_BY_CONDITIONS + SEARCH_BY_TITLE + SORT_FOR_SEARCH_QUERY;
        } else {
            throw new SqlParameterException("Search condition is not defined");
        }

        return jdbc.query(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SQLQuery);

            stmt.setString(1, "%" + query + "%");

            return stmt;
        }, new FilmRowMapper());
    }
}
