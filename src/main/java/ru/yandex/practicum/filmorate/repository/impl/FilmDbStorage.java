package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.entity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.rowmapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Film createFilm(Film film) {
        // проверка существования рейтинга
        final  String checkMpaRatingIdQuery = """
                SELECT COUNT(*)
                FROM mpa_ratings
                WHERE id = ?;
                """;

        long filmMpaRatingId = film.getMpa().getId();
        int count = jdbc.queryForObject(checkMpaRatingIdQuery, Integer.class, filmMpaRatingId);
        if (count == 0) throw new NotFoundException(String.format("MpaRating with id=%s not found", filmMpaRatingId));

        final String insertFilmQuery = """
                INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(insertFilmQuery, Statement.RETURN_GENERATED_KEYS);

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
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String checkGenreIdQuery = """
                    SELECT COUNT(*)
                    FROM genres
                    WHERE id = ?;
                    """;

            for (Genre genre : film.getGenres()) {
                long genreId = genre.getId();
                count = jdbc.queryForObject(checkGenreIdQuery, Integer.class, genreId);
                if (count == 0) throw new NotFoundException(String.format("Genre with id=%s not found", genreId));
            }

            final String insertGenreQuery = """
                    INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
                    """;

            for (Genre genre : film.getGenres()) {
                jdbc.update(insertGenreQuery, filmId, genre.getId());
            }
        }

        return film;
    }

    @Override
    public Optional<Boolean> addLike(Long filmId, Long userId) {
        String insertLikeQuery = """
                INSERT INTO film_likes (film_id, user_id) VALUES (?, ?);
                """;

        int rowsAffected = jdbc.update(insertLikeQuery, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        String query = """
                SELECT f.*,
                       m.id mpa_id,
                       m.name mpa_name,
                       m.description mpa_description
                FROM films f
                JOIN mpa_ratings m
                ON f.mpa_rating_id = m.id
                WHERE f.id = ?;
                """;

        FilmDto filmDto = jdbc.queryForObject(query, new FilmRowMapper(), id);

        if (filmDto != null) {
            // загрузить жанры
            query = """
                    SELECT g.id,
                           g.name
                    FROM genres g
                    JOIN film_genres fg
                    ON g.id = fg.genre_id
                    WHERE fg.film_id = ?;
                    """;

            List<GenreDto> genres = jdbc.query(query, (resultSet, rowNum) -> GenreDto.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .build(), id);

            filmDto.setGenres(new HashSet<>(genres));

            // загрузить лайки
            query = """
                    SELECT user_id
                    FROM film_likes
                    WHERE film_id = ?;
                    """;

            List<Long> likes = jdbc.queryForList(query, Long.class, id);
            filmDto.setLikes(new HashSet<>(likes));

            return Optional.of(FilmMapper.toFilm(filmDto));
        }

        return Optional.empty();
    }

    @Override
    public Collection<Film> getAllFilms() {
        String findFilmsQuery = """
                SELECT f.*,
                       m.id mpa_id,
                       m.name mpa_name,
                       m.description mpa_description
                FROM films f
                JOIN mpa_ratings m
                ON f.mpa_rating_id = m.id;
                """;

        List<FilmDto> filmDtos = jdbc.query(findFilmsQuery, new FilmRowMapper());

        // загрузить жанры и лайки для всех фильмов
        for (FilmDto filmDto : filmDtos) {
            String findGenresQuery = """
                    SELECT g.id,
                           g.name
                    FROM genres g
                    JOIN film_genres fg
                    ON g.id = fg.genre_id
                    WHERE fg.film_id = ?;
                    """;

            List<GenreDto> genres = jdbc.query(findGenresQuery, (resultSet, rowNum) -> GenreDto.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .build(), filmDto.getId());

            filmDto.setGenres(new HashSet<>(genres));

            String findLikesQuery = """
                    SELECT user_id
                    FROM film_likes
                    WHERE film_id = ?;
                    """;

            List<Long> likes = jdbc.queryForList(findLikesQuery, Long.class, filmDto.getId());
            filmDto.setLikes(new HashSet<>(likes));
        }

        return filmDtos.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String findPopularFilmsQuery = """
                SELECT f.*,
                       mr.id mpa_id,
                       mr.name mpa_name,
                       mr.description mpa_description,
                       COUNT(fl.user_id) likes_count
                FROM films f
                JOIN mpa_ratings mr
                ON f.mpa_rating_id = mr.id
                LEFT JOIN film_likes fl
                ON f.id = fl.film_id
                GROUP BY f.id,
                         mr.name,
                         mr.description
                ORDER BY likes_count DESC
                LIMIT ?;
                """;

        List<FilmDto> filmDtos = jdbc.query(findPopularFilmsQuery, new FilmRowMapper(), count);

        // загрузить жанры и лайки для выбранных фильмов
        for (FilmDto filmDto : filmDtos) {
            String findGenresQuery = """
                    SELECT g.id,
                           g.name
                    FROM genres g
                    JOIN film_genres fg
                    ON g.id = fg.genre_id
                    WHERE fg.film_id = ?;
                    """;

            List<GenreDto> genres = jdbc.query(findGenresQuery, (resultSet, rowNum) -> GenreDto.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .build(), filmDto.getId());

            filmDto.setGenres(new HashSet<>(genres));

            String likesQuery = """
                    SELECT user_id
                    FROM film_likes
                    WHERE film_id = ?;
                    """;

            List<Long> likes = jdbc.queryForList(likesQuery, Long.class, filmDto.getId());
            filmDto.setLikes(new HashSet<>(likes));
        }

        return filmDtos.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }

    @Override
    public Film updateFilm(Film film) {
        String checkFilmQuery = """
                SELECT COUNT(*)
                FROM films
                WHERE id = ?;
                """;

        Long filmId = film.getId();
        int count = jdbc.queryForObject(checkFilmQuery, Integer.class, filmId);
        if (count == 0) throw new NotFoundException(String.format("Film with id=%s not found", filmId));

        String query = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
                WHERE id = ?;
                """;

        jdbc.update(
                query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        // обновить жанры фильма
        query = """
                DELETE FROM film_genres
                WHERE film_id = ?;
                """;

        jdbc.update(query, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            query = """
                    INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
                    """;

            for (Genre genre : film.getGenres()) {
                jdbc.update(query, film.getId(), genre.getId());
            }
        }

        return film;
    }

    @Override
    public Optional<Boolean> removeLike(Long filmId, Long userId) {
        String query = """
                DELETE FROM film_likes
                WHERE film_id = ?
                AND user_id = ?;
                """;

        int rowsAffected = jdbc.update(query, filmId, userId);
        return Optional.of(rowsAffected > 0);
    }
}
