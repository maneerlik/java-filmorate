package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.mapper.entity.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.rowmapper.DirectorDtoRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Primary
@Slf4j
public class DirectorDbStorage extends BaseDbStorage implements DirectorStorage {
    public static final String INSERT_DIRECTOR_QUERY = """
            INSERT INTO directors (name) VALUES (?);
            """;

    public static final String FIND_ALL_DIRECTORS_QUERY = """
            SELECT * FROM directors;
            """;

    public static final String FIND_DIRECTOR_BY_ID = """
            SELECT *
            FROM directors
            WHERE id = ?;
            """;

    public static final String UPDATE_DIRECTOR_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE id = ?;
            """;

    private static final String DELETE_DIRECTOR_QUERY = """
            DELETE FROM directors
            WHERE id = ?;
            """;


    public DirectorDbStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    //--- Создать режиссера --------------------------------------------------------------------------------------------
    @Override
    public Director createDirector(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_DIRECTOR_QUERY, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, director.getName());

            return stmt;
        }, keyHolder);

        Long directorId = keyHolder.getKeyAs(Long.class);
        director.setId(directorId);
        log.info("Successfully created director with id: {}", directorId);

        return director;
    }

    //--- Получить режиссера по id -------------------------------------------------------------------------------------
    @Override
    public Optional<Director> getDirector(Long id) {
        Objects.requireNonNull(id, "Director id cannot be null");
        checkEntityExists(id, EntityType.DIRECTOR);

        DirectorDto directorDto = jdbc.queryForObject(FIND_DIRECTOR_BY_ID, new DirectorDtoRowMapper(), id);

        if (directorDto != null) {
            return Optional.of(DirectorMapper.toDirector(directorDto));
        }

        log.warn("Director with id {} not found", id);
        return Optional.empty();
    }

    //--- Получить список всех режиссеров ------------------------------------------------------------------------------
    @Override
    public Collection<Director> getAllDirectors() {
        List<DirectorDto> allDirector = jdbc.query(FIND_ALL_DIRECTORS_QUERY, new DirectorDtoRowMapper());

        return allDirector.stream()
                .map(DirectorMapper::toDirector)
                .toList();
    }

    //--- Обновить режиссера -------------------------------------------------------------------------------------------
    @Override
    public Optional<Director> updateDirector(Director director) {
        Objects.requireNonNull(director.getId(), "Director id cannot be null");

        int rowsAffected = jdbc.update(
                UPDATE_DIRECTOR_QUERY,
                director.getName(),
                director.getId()
        );

        if (rowsAffected > 0) {
            log.info("Successfully updated director with id: {}", director.getId());
            return Optional.of(director);
        }

        log.warn("No director found with id: {} for update", director.getId());
        return Optional.empty();
    }

    //--- Удалить режиссера --------------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeDirector(Long id) {
        checkEntityExists(id, EntityType.DIRECTOR);

        int rowsAffected = jdbc.update(DELETE_DIRECTOR_QUERY, id);
        return Optional.of(rowsAffected > 0);
    }
}

