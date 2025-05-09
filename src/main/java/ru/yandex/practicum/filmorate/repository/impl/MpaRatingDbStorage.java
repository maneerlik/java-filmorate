package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.MpaRatingStorage;
import ru.yandex.practicum.filmorate.rowmapper.MpaRatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbc;

    public MpaRatingDbStorage(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Optional<MpaRating> getMpaRating(Long id) {
        String checkMpaRatingIdQuery = """
                SELECT COUNT(*)
                FROM mpa_ratings
                WHERE id = ?;
                """;

        int count = jdbc.queryForObject(checkMpaRatingIdQuery, Integer.class, id);
        if (count == 0) throw new NotFoundException(String.format("MpaRating with id=%s not found", id));

        String findMpaRatingQuery = """
                SELECT *
                FROM mpa_ratings
                WHERE id = ?;
                """;

        MpaRating mpaRating = jdbc.queryForObject(findMpaRatingQuery, new MpaRatingRowMapper(), id);
        return Optional.ofNullable(mpaRating);
    }

    @Override
    public Collection<MpaRating> getAllMpaRatings() {
        String findMpaRatingsAll = """
                SELECT *
                FROM mpa_ratings;
                """;

        return jdbc.query(findMpaRatingsAll, new MpaRatingRowMapper());
    }
}
