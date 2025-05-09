package ru.yandex.practicum.filmorate.mapper.entity;

import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public class MpaRatingMapper {
    public static MpaRating toMpaRating(MpaRatingDto mpaRatingDto) {
        MpaRating mpaRating = new MpaRating();

        mpaRating.setId(mpaRatingDto.getId());
        mpaRating.setName(mpaRatingDto.getName());
        mpaRating.setDescription(mpaRatingDto.getDescription());

        return mpaRating;
    }
}
