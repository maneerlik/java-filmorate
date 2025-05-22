package ru.yandex.practicum.filmorate.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static ru.yandex.practicum.filmorate.util.TestDataGenerator.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class})
@ActiveProfiles("test")
public abstract class BaseDbStorageTest {

    @Autowired
    protected UserDbStorage userStorage;

    @Autowired
    protected FilmDbStorage filmStorage;

    protected User user;
    protected Film film;

    // user
    protected String email;
    protected String login;
    protected String userName;
    protected LocalDate birthday;
    protected Set<Long> friends;

    // film
    protected String filmName;
    protected String description;
    protected LocalDate releaseDate;
    protected Long duration;
    protected MpaRating mpa;
    protected Set<Genre> genres;
    protected Set<Director> directors;
    protected Set<Long> likes;


    protected void setUp() {
        email = getRandomEmail();
        login = getRandomLogin();
        userName = getRandomUserName();
        birthday = getRandomPastDate();

        user = new User(null, email, login, userName, birthday, friends);

        filmName = getRandomFilmName();
        description = getRandomDescription();
        releaseDate = getRandomReleaseDate();
        duration = getRandomDuration();
        mpa = getRandomMpa();

        film = new Film(null, filmName, description, releaseDate, duration, mpa, genres, directors, likes);
    }
}
