package ru.yandex.practicum.filmorate.mapper.entity;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {
    public static Director toDirector(DirectorDto directorDto) {
        Director director = new Director();

        director.setId(directorDto.getId());
        director.setName(directorDto.getName());

        return director;
    }
}