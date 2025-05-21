package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film createFilm(Film film);

    Optional<Boolean> addLike(Long filmId, Long userId);

    Optional<Film> getFilm(Long id);

    Collection<Film> getAllFilms();

    Collection<Film> getPopularFilms(int count);

    Film updateFilm(Film film);

    Optional<Boolean> removeLike(Long filmId, Long userId);
}
