package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.LikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.SqlParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Validated
public class FilmService {

    private static final int COUNT_ZERO = 0;
    private static final int ID_GENRE_ZERO = 0;
    private static final int YEAR_ZERO = 0;
    private static final int COUNT_DEFAULT = 10;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedService feedService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, FeedService feedService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.feedService = feedService;
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
        feedService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        log.info("Like added to film {} by user {}", filmId, userId);
    }

    public Optional<Film> getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Collection<Film> getPopularFilms(int count, Long genreId, int year) {
        if (genreId == ID_GENRE_ZERO && year == YEAR_ZERO) {
            if (count == COUNT_ZERO) {
                count = COUNT_DEFAULT;
            }
            if (count <= COUNT_ZERO) {
                throw new IllegalArgumentException("Count must be positive");
            }
            return filmStorage.getPopularFilms(count);
        } else {
            if (count < COUNT_ZERO) {
                throw new IllegalArgumentException("Count must be positive");
            }
            return filmStorage.getPopularFilmsByGenreAndYear(count, genreId, year);
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
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
        feedService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        log.debug("Like removed from film {} by user {}", filmId, userId);
    }

    private void validateFilmAndUserExist(Long filmId, Long userId) {
        if (filmStorage.getFilm(filmId).isEmpty()) {
            throw new NotFoundException("Film not found");
        }
        if (userStorage.getUser(userId).isEmpty()) {
            throw new NotFoundException("User not found");
        }
    }

    public Collection<Film> getFilmsDirector(Long id, String sortBy) {
        return filmStorage.getFilmsDirector(id, sortBy);
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        if (by.isEmpty()) {
            throw new SqlParameterException("Search condition is not defined");
        }

        by.replaceAll(String::toUpperCase);
        return filmStorage.searchFilms(query, by);
    }

    public void deleteFilmById(Long filmId) {
        filmStorage.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found"));
        filmStorage.deleteFilmById(filmId);
    }
}
