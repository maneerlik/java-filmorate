package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
public class FilmService {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1L;

    public Film create(@Valid Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    public Film update(@Validated(UpdateValidationGroup.class) Film newFilm) {
        if (!films.containsKey(newFilm.getId()))
            throw new NotFoundException(String.format("Film with id=%s not found", newFilm.getId()));

        Film oldFilm = films.get(newFilm.getId());
        if (nonNull(newFilm.getName())) oldFilm.setName(newFilm.getName());
        if (nonNull(newFilm.getDescription())) oldFilm.setDescription(newFilm.getDescription());
        if (nonNull(newFilm.getReleaseDate())) oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (nonNull(newFilm.getDuration())) oldFilm.setDuration(newFilm.getDuration());
        return oldFilm;
    }

    public Collection<Film> findAll() {
        return films.values();
    }
}
