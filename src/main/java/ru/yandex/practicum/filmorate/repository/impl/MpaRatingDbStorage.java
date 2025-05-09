package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.EntityType;
import ru.yandex.practicum.filmorate.repository.MpaRatingStorage;
import ru.yandex.practicum.filmorate.rowmapper.MpaRatingRowMapper;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Реализация хранилища рейтингов MPA (Motion Picture Association) в базе данных.
 * Предоставляет методы для работы с возрастными рейтингами фильмов:
 * - получение рейтинга по идентификатору
 * - получение списка всех доступных рейтингов
 *
 * Наследует базовую функциональность проверки существования сущностей из BaseDbStorage.
 *
 * @Repository - указывает, что класс является компонентом Spring Data Access Layer
 */

@Repository
public class MpaRatingDbStorage extends BaseDbStorage implements MpaRatingStorage {
    public static final String FIND_MPA_RATING_BY_ID = """
            SELECT *
            FROM mpa_ratings
            WHERE id = ?;
            """;

    public static final String FIND_ALL_MPA_RATINGS = """
            SELECT * FROM mpa_ratings;
            """;

    public MpaRatingDbStorage(final JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Получить MPA рейтинг по id -----------------------------------------------------------------------------------
    @Override
    public Optional<MpaRating> getMpaRating(Long id) {
        Objects.requireNonNull(id, "MPA rating id cannot be null");
        checkMpaRatingExists(id);
        MpaRating mpaRating = jdbc.queryForObject(FIND_MPA_RATING_BY_ID, new MpaRatingRowMapper(), id);
        return Optional.ofNullable(mpaRating);
    }

    //--- Получить все рейтинги ----------------------------------------------------------------------------------------
    @Override
    public Collection<MpaRating> getAllMpaRatings() {
        return jdbc.query(FIND_ALL_MPA_RATINGS, new MpaRatingRowMapper());
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private void checkMpaRatingExists(Long ratingId) {
        checkEntityExists(ratingId, EntityType.MPA_RATING);
    }
}
