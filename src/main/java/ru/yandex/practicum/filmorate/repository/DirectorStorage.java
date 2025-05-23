package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Director createDirector(Director director);

    Optional<Director> getDirector(Long id);

    Collection<Director> getAllDirectors();

    Optional<Director> updateDirector(Director director);

    Optional<Boolean> removeDirector(Long id);
}