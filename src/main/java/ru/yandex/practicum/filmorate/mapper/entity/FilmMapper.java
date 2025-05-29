package ru.yandex.practicum.filmorate.mapper.entity;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {

    public static Film toFilm(FilmDto filmDto) {
        Film film = new Film();

        film.setId(filmDto.getId());
        film.setName(filmDto.getName());
        film.setDescription(filmDto.getDescription());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDuration(filmDto.getDuration());
        film.setMpa(MpaRatingMapper.toMpaRating(filmDto.getMpa()));
        film.setGenres(filmDto.getGenres().stream()
                .map(GenreMapper::toGenre)
                .toList());
        film.setDirectors(filmDto.getDirectors().stream()
                .map(DirectorMapper::toDirector)
                .toList());
        film.setLikes(filmDto.getLikes());

        return film;
    }
}
