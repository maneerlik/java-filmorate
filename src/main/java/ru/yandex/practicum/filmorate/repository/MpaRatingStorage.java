package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

public interface MpaRatingStorage {
    Optional<MpaRating> getMpaRating(Long id);

    Collection<MpaRating> getAllMpaRatings();
}
