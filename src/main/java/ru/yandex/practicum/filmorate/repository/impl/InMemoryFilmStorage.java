/*
package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.*;

import static java.util.Objects.nonNull;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 1L;


    @Override
    public Film createFilm(Film film) {
        film.setId(id);
        films.put(id++, film);
        return film;
    }

    @Override
    public Optional<Boolean> addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) return Optional.empty();

        boolean added = film.getLikes().add(userId);
        return Optional.of(added);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        if (films.isEmpty()) return Collections.emptyList();

        return films.values().stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()
                ))
                .limit(count)
                .toList();
    }

    @Override
    public Optional<Film> getFilm(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film updateFilm(Film film) {
        long filmId = film.getId();
        Film existFilm = films.get(filmId);

        if (existFilm == null) throw new NotFoundException(String.format("Film with id=%s not found", filmId));

        Film updatedFilm = new Film();

        updatedFilm.setId(filmId);
        updatedFilm.setName(getOrDefault(film.getName(), existFilm.getName()));
        updatedFilm.setDescription(getOrDefault(film.getDescription(), existFilm.getDescription()));
        updatedFilm.setReleaseDate(getOrDefault(film.getReleaseDate(), existFilm.getReleaseDate()));
        updatedFilm.setDuration(getOrDefault(film.getDuration(), existFilm.getDuration()));

        films.put(filmId, updatedFilm);

        return updatedFilm;
    }

    @Override
    public Optional<Boolean> removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) return Optional.empty();

        boolean removed = film.getLikes().remove(userId);
        return Optional.of(removed);
    }

    @Override
    public Collection<Film> getFilmsDirector(Long filmId, String sortBy) {
        return List.of();
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return nonNull(value) ? value : defaultValue;
    }
}
*/
