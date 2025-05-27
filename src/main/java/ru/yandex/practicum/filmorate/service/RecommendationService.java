package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.RecommendationStorage;

import java.util.Collection;

@Service
@Validated
@AllArgsConstructor
public class RecommendationService {
    private final RecommendationStorage recommendationStorage;

    public Collection<Film> getRecommendations(Long userId) {
        return recommendationStorage.getRecommendations(userId);
    }

}
