package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@RestController
@Slf4j
@Validated
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1L;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Film created: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Validated(UpdateValidationGroup.class) @RequestBody Film newFilm) {
        if (!films.containsKey(newFilm.getId()))
            throw new NotFoundException(String.format("Film with id=%s not found", newFilm.getId()));

        Film oldFilm = films.get(newFilm.getId());
        if (nonNull(newFilm.getName())) oldFilm.setName(newFilm.getName());
        if (nonNull(newFilm.getDescription())) oldFilm.setDescription(newFilm.getDescription());
        if (nonNull(newFilm.getReleaseDate())) oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (nonNull(newFilm.getDuration())) oldFilm.setDuration(newFilm.getDuration());
        log.info("Film updated: {}", oldFilm);
        return oldFilm;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }
}
