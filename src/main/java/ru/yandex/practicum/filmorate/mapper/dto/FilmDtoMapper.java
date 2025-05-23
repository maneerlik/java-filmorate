package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.stream.Collectors;

public class FilmDtoMapper {
    public static FilmDto toFilmDto(Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(MpaRatingDtoMapper.toMpaRatingDto(film.getMpa()))
                .genres(film.getGenres().stream()
                        .map(GenreDtoMapper::toGenreDto)
                        .collect(Collectors.toSet()))
                .likes(film.getLikes())
                .directors(film.getDirectors().stream()
                        .map(DirectorDtoMapper::toDirectorDto)
                        .collect(Collectors.toSet()))
                .build();
    }
}
