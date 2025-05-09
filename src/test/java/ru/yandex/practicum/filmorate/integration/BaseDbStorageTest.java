package ru.yandex.practicum.filmorate.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.time.LocalDate;
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
    protected String user_name;
    protected LocalDate birthday;
    protected Set<Long> friends;

    // film
    protected String film_name;
    protected String description;
    protected LocalDate release_date;
    protected Long duration;
    protected MpaRating mpa;
    protected Set<Genre> genres;
    protected Set<Long> likes;


    protected void setUp() {
        email = getRandomEmail();
        login = getRandomLogin();
        user_name = getRandomUserName();
        birthday = getRandomPastDate();

        user = new User(null, email, login, user_name, birthday, friends);

        film_name = getRandomFilmName();
        description = getRandomDescription();
        release_date = getRandomReleaseDate();
        duration = getRandomDuration();
        mpa = getRandomMpa();

        film = new Film(null, film_name, description, release_date, duration, mpa, genres, likes);
    }
}
