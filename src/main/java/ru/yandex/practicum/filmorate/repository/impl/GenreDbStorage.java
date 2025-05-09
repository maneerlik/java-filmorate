package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.rowmapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;

    public GenreDbStorage(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Optional<Genre> getGenre(Long id) {
        String checkGenresIdQuery = """
                SELECT COUNT(*)
                FROM genres
                WHERE id = ?;
                """;

        int count = jdbc.queryForObject(checkGenresIdQuery, Integer.class, id);
        if (count == 0) throw new NotFoundException(String.format("Genre with id=%s not found", id));

        String findGenreQuery = """
                SELECT *
                FROM genres
                WHERE id = ?;
                """;

        Genre genre = jdbc.queryForObject(findGenreQuery, new GenreRowMapper(), id);
        return Optional.ofNullable(genre);
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String findGenriesQuery = """
                SELECT *
                FROM genres;
                """;

        return jdbc.query(findGenriesQuery, new GenreRowMapper());
    }
}
