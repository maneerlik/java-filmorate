package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.LikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@Validated
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }


    public Film create(@Valid Film film) {
        Film createdFilm = filmStorage.createFilm(film);
        log.info("Film created: {}", createdFilm);
        return createdFilm;
    }

    public void addLike(Long filmId, Long userId) {
        validateFilmAndUserExist(filmId, userId);
        filmStorage.addLike(filmId, userId)
                .orElseThrow(() -> new LikeException("Failed to add like"));
        log.info("Like added to film {} by user {}", filmId, userId);
    }

    public Optional<Film> getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) throw new IllegalArgumentException("Count must be positive");
        return filmStorage.getPopularFilms(count);
    }

    public Film update(@Validated(UpdateValidationGroup.class) Film film) {
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Film updated: {}", updatedFilm);
        return updatedFilm;
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmAndUserExist(filmId, userId);
        filmStorage.removeLike(filmId, userId)
                .orElseThrow(() -> new LikeException("Like not found"));
        log.debug("Like removed from film {} by user {}", filmId, userId);
    }

    private void validateFilmAndUserExist(Long filmId, Long userId) {
        if (filmStorage.getFilm(filmId).isEmpty()) throw new NotFoundException("Film not found");
        if (userStorage.getUser(userId).isEmpty()) throw new NotFoundException("User not found");
    }

    public void deleteFilmById(Long filmId) {
        filmStorage.getFilm(filmId)
            .orElseThrow(() -> new NotFoundException("Film not found"));
        filmStorage.deleteFilmById(filmId);
    }
}
