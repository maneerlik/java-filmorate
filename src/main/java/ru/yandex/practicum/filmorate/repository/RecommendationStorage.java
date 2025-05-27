package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface RecommendationStorage {
    Collection<Film> getRecommendations(Long userId);
}
