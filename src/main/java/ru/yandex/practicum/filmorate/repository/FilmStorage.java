package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

  Film createFilm(Film film);

  Optional<Boolean> addLike(Long filmId, Long userId);

  Optional<Film> getFilm(Long id);

  Collection<Film> getAllFilms();

  Collection<Film> getPopularFilms(int count);

  Collection<Film> getCommonFilms(Long userId1, Long userId2);

  Collection<Film> getPopularFilmsByGenreAndYear(int count, Long genreId, int year);

  Film updateFilm(Film film);

  Optional<Boolean> removeLike(Long filmId, Long userId);

  Collection<Film> getFilmsDirector(Long filmId, String sortBy);

  Collection<Film> searchFilms(String query, List<String> searchParameters);

  void deleteFilmById(Long filmId);

  Collection<Film> getRecommendations(Long userId);
}
