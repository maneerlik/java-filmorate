package ru.yandex.practicum.filmorate.mapper.entity;

import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {

    public static Review toReview(ReviewDto reviewDto) {
        Review review = new Review();

        review.setReviewId(reviewDto.getId());
        review.setContent(reviewDto.getContent());
        review.setIsPositive(reviewDto.getIsPositive());
        review.setUserId(reviewDto.getUserId());
        review.setFilmId(reviewDto.getFilmId());
        review.setUseful(reviewDto.getUseful());

        return review;
    }
}
