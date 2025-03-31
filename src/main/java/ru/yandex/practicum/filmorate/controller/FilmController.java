package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@Validated
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }


    @PostMapping
    public Film create(@RequestBody Film film) {
        Film filmCreated = filmService.create(film);
        log.info("Film created: {}", filmCreated);
        return filmCreated;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        Film filmUpdated = filmService.update(newFilm);
        log.info("Film updated: {}", filmUpdated);
        return filmUpdated;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }
}
