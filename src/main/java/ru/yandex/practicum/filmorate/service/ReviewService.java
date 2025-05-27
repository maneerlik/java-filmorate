package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.LikeException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@Validated
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
    }


    public Review createReview(@Valid Review review) {
        return reviewStorage.createReview(review);
    }

    public Optional<Review> getReview(Long id) {
        return reviewStorage.getReview(id);
    }

    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long id) {
        reviewStorage.deleteReview(id)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        log.debug("Review {} removed", id);
    }

    public void addLike(Long reviewId, Long userId) {
        validateReviewAndUserExist(reviewId, userId);
        reviewStorage.addLike(reviewId, userId)
                .orElseThrow(() -> new LikeException("Like not added"));
        log.debug("Like added from review {} by user {}", reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewAndUserExist(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId)
                .orElseThrow(() -> new LikeException("Dislike not added"));
        log.debug("Dislike added from review {} by user {}", reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateReviewAndUserExist(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId)
                .orElseThrow(() -> new LikeException("Like not removed"));
        log.debug("Like removed from review {} by user {}", reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateReviewAndUserExist(reviewId, userId);
        reviewStorage.removeDislike(reviewId, userId)
                .orElseThrow(() -> new LikeException("Dislike not removed"));
        log.debug("Dislike removed from review {} by user {}", reviewId, userId);
    }

    private void validateReviewAndUserExist(Long reviewId, Long userId) {
        if (reviewStorage.getReview(reviewId).isEmpty()) throw new NotFoundException("Film not found");
        if (userStorage.getUser(userId).isEmpty()) throw new NotFoundException("User not found");
    }
}
