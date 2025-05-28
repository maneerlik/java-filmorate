package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

  Review createReview(Review review);

  Optional<Review> getReview(Long id);

  Collection<Review> getReviewsByFilmId(Long filmId, int count);

  Review updateReview(Review review);

  Optional<Boolean> deleteReview(Long id);

  Optional<Boolean> addLike(Long reviewId, Long userId);

  Optional<Boolean> addDislike(Long reviewId, Long userId);

  Optional<Boolean> removeLike(Long reviewId, Long userId);

  Optional<Boolean> removeDislike(Long reviewId, Long userId);
}
