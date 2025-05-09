package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public class MpaRatingDtoMapper {
    public static MpaRatingDto toMpaRatingDto(MpaRating mpaRating) {
        return MpaRatingDto.builder()
                .id(mpaRating.getId())
                .name(mpaRating.getName())
                .description(mpaRating.getDescription())
                .build();
    }
}
