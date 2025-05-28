package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }


  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Review createReview(@RequestBody Review review) {
    return reviewService.createReview(review);
  }

  @GetMapping("/{id}")
  public Review getReviewById(@PathVariable Long id) {
    return reviewService.getReview(id)
        .orElseThrow(() -> new NotFoundException("Review not found"));
  }

  @GetMapping
  public Collection<Review> findReviewsByFilmId(
      @RequestParam(required = false) Long filmId,
      @RequestParam(defaultValue = "10") Integer count
  ) {
    return reviewService.getReviewsByFilmId(filmId, count);
  }

  @PutMapping
  public Review updateReview(@RequestBody Review review) {
    return reviewService.updateReview(review);
  }

  @PutMapping("/{id}/like/{userId}")
  public void addLike(
      @PathVariable Long id,
      @PathVariable Long userId
  ) {
    reviewService.addLike(id, userId);
  }

  @PutMapping("/{id}/dislike/{userId}")
  public void addDislike(
      @PathVariable Long id,
      @PathVariable Long userId
  ) {
    reviewService.addDislike(id, userId);
  }

  @DeleteMapping("/{id}")
  public void deleteReview(@PathVariable Long id) {
    reviewService.deleteReview(id);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public void removeLike(
      @PathVariable Long id,
      @PathVariable Long userId
  ) {
    reviewService.removeLike(id, userId);
  }

  @DeleteMapping("/{id}/dislike/{userId}")
  public void removeDislike(
      @PathVariable Long id,
      @PathVariable Long userId
  ) {
    reviewService.removeDislike(id, userId);
  }
}
