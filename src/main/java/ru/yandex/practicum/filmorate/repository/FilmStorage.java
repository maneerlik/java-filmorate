package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumeration.SearchParameter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film createFilm(Film film);

    Optional<Boolean> addLike(Long filmId, Long userId);

    Optional<Film> getFilm(Long id);

    Collection<Film> getAllFilms();

    Collection<Film> getPopularFilms(int count);

    Film updateFilm(Film film);

    Optional<Boolean> removeLike(Long filmId, Long userId);

    Collection<Film> searchFilms(String query, List<SearchParameter> searchParameters);

    Collection<Film> getFilmsDirector(Long filmId, String sortBy);

    void deleteFilmById(Long filmId);
}
