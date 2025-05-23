package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.addLike(id, userId);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        return filmService.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Film not found"));
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count
    ) {
        return filmService.getPopularFilms(count);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        Film filmUpdated = filmService.update(newFilm);
        log.info("Film updated: {}", filmUpdated);
        return filmUpdated;
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.removeLike(id, userId);
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilmById(@PathVariable Long filmId) {
        filmService.deleteFilmById(filmId);
        return ResponseEntity.ok().build();
    }
}
