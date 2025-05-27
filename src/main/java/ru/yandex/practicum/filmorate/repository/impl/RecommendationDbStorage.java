package ru.yandex.practicum.filmorate.repository.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.entity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.RecommendationStorage;
import ru.yandex.practicum.filmorate.rowmapper.DirectorDtoRowMapper;
import ru.yandex.practicum.filmorate.rowmapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.rowmapper.GenreDtoRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage.FIND_GENRES_ID_BY_FILM_ID_QUERY;
import static ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage.FIND_LIKES_BY_FILM_ID_QUERY;
import static ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage.FIND_DIRECTORS_ID_BY_FILM_ID_QUERY;

@Repository
@AllArgsConstructor
public class RecommendationDbStorage implements RecommendationStorage {

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
                        WHERE film_id IN (?)
                        GROUP BY user_id) u_cl
                    RIGHT JOIN (
                        SELECT user_id, COUNT(*) all_likes
                        FROM film_likes
                        GROUP BY user_id) u_al ON u_al.user_id = u_cl.user_id)
                    WHERE (all_likes - common_likes > 0))
            AND f.id NOT IN (?));
            """;

    private static final String FIND_LIST_OF_FILM_ID_BY_USER_ID = """
            SELECT film_id
            FROM film_likes
            WHERE user_id = ?;
            """;

    private final JdbcTemplate jdbc;

    @Override
    public Collection<Film> getRecommendations(Long userId) {

        // Найти лайки пользователя
        final List<Long> usersLikes = jdbc.queryForList(FIND_LIST_OF_FILM_ID_BY_USER_ID, Long.class, userId);
        if (usersLikes.isEmpty()) {
            return List.of();
        }

        List<FilmDto> recommendation = jdbc.query(connection -> {
            PreparedStatement stmt = connection.prepareStatement(FIND_LIST_OF_USER_ID_FOR_RECOMMENDATION);

            stmt.setArray(1, connection.createArrayOf("BIGINT", usersLikes.toArray()));
            stmt.setArray(2, connection.createArrayOf("BIGINT", usersLikes.toArray()));

            return stmt;
        }, new FilmRowMapper());

        // Лайки, жанры, режиссеры фильмов
        for (FilmDto filmDto: recommendation) {
            long filmId = filmDto.getId();
            filmDto.setGenres(new HashSet<>(
                    jdbc.query(FIND_GENRES_ID_BY_FILM_ID_QUERY, new GenreDtoRowMapper(), filmId)));
            filmDto.setDirectors(new HashSet<>(
                    jdbc.query(FIND_DIRECTORS_ID_BY_FILM_ID_QUERY, new DirectorDtoRowMapper(), filmId)));
            filmDto.setLikes(new HashSet<>(
                    jdbc.queryForList(FIND_LIKES_BY_FILM_ID_QUERY, Long.class, filmId)));
        }

        return recommendation.stream()
                .map(FilmMapper::toFilm)
                .toList();
    }
}
