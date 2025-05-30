package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.entity.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.rowmapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Slf4j
public class ReviewDbStorage extends BaseDbStorage implements ReviewStorage {

    public static final String INSERT_VOTE_FOR_REVIEW = """
            INSERT INTO review_votes (review_id, user_id, vote_type) VALUES (?, ?, ?);
            """;
    public static final String SELECT_REVIEW_BY_ID = """
            SELECT * FROM reviews WHERE id = ?;
            """;
    public static final String SELECT_REVIEWS_BY_FILM_ID = """
            SELECT * FROM reviews WHERE film_id = ? ORDER BY rating DESC LIMIT ?;
            """;
    public static final String SELECT_REVIEWS_WITHOUT_FILM_ID = """
            SELECT * FROM reviews ORDER BY rating DESC LIMIT ?;
            """;
    public static final String SELECT_RATING_OF_REVIEW = """
            SELECT rating FROM reviews WHERE id = ?;
            """;
    public static final String CHECK_VOTE_EXISTS_FOR_UPDATE = """
            SELECT EXISTS(
                SELECT 1
                FROM review_votes
                WHERE review_id = ?
                AND user_id = ?
                AND vote_type <> ?
            );
            """;
    public static final String UPDATE_REVIEW_CONTENT_AND_TYPE = """
            UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?;
            """;
    public static final String RECALCULATE_RATING_ON_VOTE_UPDATE = """
            UPDATE reviews
            SET rating =
                CASE
                    WHEN rating + ? = 0 THEN ?
                    ELSE rating + ?
                END
            WHERE id = ?;
            """;
    public static final String UPDATE_REVIEW_RATING = """
            UPDATE reviews SET rating = rating + ? WHERE id = ?;
            """;
    public static final String UPDATE_VOTE_TYPE = """
            UPDATE review_votes SET vote_type = ? WHERE review_id = ? AND user_id = ?;
            """;
    public static final String DELETE_REVIEW_BY_ID = """
            DELETE FROM reviews WHERE id = ?;
            """;
    public static final String DELETE_VOTE_BY_REVIEW_USER_AND_TYPE = """
            DELETE FROM review_votes WHERE review_id = ? AND user_id = ? AND vote_type = ?;
            """;
    private static final String INSERT_REVIEW = """
            INSERT INTO reviews (user_id, film_id, content, is_positive) VALUES (?, ?, ?, ?);
            """;


    public ReviewDbStorage(final JdbcTemplate jdbc) {
        super(jdbc);
    }


    //--- Создание отзыва ----------------------------------------------------------------------------------------------
    @Override
    public Review createReview(Review review) {
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_REVIEW,
                    Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, review.getUserId());
            stmt.setLong(2, review.getFilmId());
            stmt.setString(3, review.getContent());
            stmt.setBoolean(4, review.getIsPositive());

            return stmt;
        }, keyHolder);

        Long reviewId = keyHolder.getKeyAs(Long.class);
        review.setReviewId(reviewId);

        log.info("Created new review with id: {}", reviewId);
        return review;
    }

    //--- Получение отзыва по id ---------------------------------------------------------------------------------------
    @Override
    public Optional<Review> getReview(Long id) {
        checkReviewExists(id);
        ReviewDto reviewDto = jdbc.queryForObject(SELECT_REVIEW_BY_ID, new ReviewRowMapper(), id);
        return Optional.ofNullable(reviewDto).map(ReviewMapper::toReview);
    }

    //--- Получение списка отзывов по id фильма ------------------------------------------------------------------------
    @Override
    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        List<ReviewDto> reviews;

        if (Objects.isNull(filmId)) {
            reviews = jdbc.query(SELECT_REVIEWS_WITHOUT_FILM_ID, new ReviewRowMapper(), count);
        } else {
            reviews = jdbc.query(SELECT_REVIEWS_BY_FILM_ID, new ReviewRowMapper(), filmId, count);
        }

        return reviews.stream().map(ReviewMapper::toReview).toList();
    }

    //--- Обновление отзыва --------------------------------------------------------------------------------------------
    @Override
    public Review updateReview(Review review) {
        checkReviewExists(review);

        jdbc.update(
                UPDATE_REVIEW_CONTENT_AND_TYPE,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        Integer useful = jdbc.queryForObject(SELECT_RATING_OF_REVIEW, Integer.class,
                review.getReviewId());
        review.setUseful(useful);

        ReviewDto updatedReview = jdbc.queryForObject(SELECT_REVIEW_BY_ID, new ReviewRowMapper(), review.getReviewId());

        return ReviewMapper.toReview(updatedReview);
    }

    //--- Удаление отзыва по id ----------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> deleteReview(Long id) {
        int rowsAffected = jdbc.update(DELETE_REVIEW_BY_ID, id);
        return Optional.of(rowsAffected > 0);
    }

    //--- Добавление лайка отзыву --------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> addLike(Long reviewId, Long userId) {
        if (isVoteExistsForUpdate(reviewId, userId, 1)) {
            return Optional.of(updateVoteTypeForReview(reviewId, userId, 1));
        }
        return Optional.of(addVoteForReview(reviewId, userId, 1));
    }

    //--- Добавление дизлайка отзыву -----------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> addDislike(Long reviewId, Long userId) {
        if (isVoteExistsForUpdate(reviewId, userId, -1)) {
            return Optional.of(updateVoteTypeForReview(reviewId, userId, -1));
        }
        return Optional.of(addVoteForReview(reviewId, userId, -1));
    }

    //--- Удаление лайка у отзыва --------------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeLike(Long reviewId, Long userId) {
        return Optional.of(removeVoteForReview(reviewId, userId, 1));
    }

    //--- Удаление дизлайка у отзыва -----------------------------------------------------------------------------------
    @Override
    public Optional<Boolean> removeDislike(Long reviewId, Long userId) {
        return Optional.of(removeVoteForReview(reviewId, userId, -1));
    }


    //--- Вспомогательные методы ---------------------------------------------------------------------------------------
    private boolean updateReviewRatingAfterVoteUpdate(int vote, Long reviewId) {
        int rowsAffected = jdbc.update(RECALCULATE_RATING_ON_VOTE_UPDATE, vote, vote, vote, reviewId);
        return rowsAffected > 0;
    }

    private boolean updateReviewRatingAfterVoteDelete(int vote, Long reviewId) {
        int rowsAffected = jdbc.update(UPDATE_REVIEW_RATING, -vote, reviewId);
        return rowsAffected > 0;
    }

    private boolean updateVoteTypeForReview(Long reviewId, Long userId, int voteType) {
        int rowsAffected = jdbc.update(UPDATE_VOTE_TYPE, voteType, reviewId, userId);
        if (rowsAffected > 0 && updateReviewRatingAfterVoteUpdate(voteType, reviewId)) {
            log.info("Updated vote type user {} to review with id {}", userId, reviewId);
            return true;
        }
        return false;
    }

    private boolean addVoteForReview(Long reviewId, Long userId, int voteType) {
        int rowsAffected = jdbc.update(INSERT_VOTE_FOR_REVIEW, reviewId, userId, voteType);
        if (rowsAffected > 0 && updateReviewRatingAfterVoteUpdate(voteType, reviewId)) {
            log.info("Inserted vote user {} to review with id {}", userId, reviewId);
            return true;
        }
        return false;
    }

    private boolean removeVoteForReview(Long reviewId, Long userId, int voteType) {
        int rowsAffected = jdbc.update(DELETE_VOTE_BY_REVIEW_USER_AND_TYPE, reviewId, userId, voteType);
        if (rowsAffected > 0 && updateReviewRatingAfterVoteDelete(voteType, reviewId)) {
            log.info("Deleted vote user {} to review with id {}", userId, reviewId);
            return true;
        }
        return false;
    }

    private boolean isVoteExistsForUpdate(Long reviewId, Long userId, int voteType) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                CHECK_VOTE_EXISTS_FOR_UPDATE, Boolean.class, reviewId, userId, voteType));
    }
}
