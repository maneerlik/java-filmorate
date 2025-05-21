package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.MpaRatingStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class MpaRatingService {
    private final MpaRatingStorage mpaRatingStorage;

    public MpaRatingService(MpaRatingStorage mpaRatingStorage) {
        this.mpaRatingStorage = mpaRatingStorage;
    }


    public Optional<MpaRating> getMpaRating(Long id) {
        return mpaRatingStorage.getMpaRating(id);
    }

    public Collection<MpaRating> findAll() {
        return mpaRatingStorage.getAllMpaRatings();
    }
}
