package ru.yandex.practicum.filmorate.util;

import net.datafaker.Faker;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public final class TestDataGenerator {
    private static final Faker FAKER = new Faker();

    /**
     * Don't let anyone instantiate this class.
     */
    private TestDataGenerator() {

    }


    public static String getRandomEmail() {
        return FAKER.internet().emailAddress();
    }

    public static String getRandomLogin() {
        return FAKER.name().firstName().toLowerCase() + FAKER.number().digits(3);
    }

    public static String getRandomUserName() {
        return FAKER.name().fullName();
    }

    public static LocalDate getRandomPastDate() {
        LocalDate now = LocalDate.now();

        long minDay = now.minusYears(100).toEpochDay();
        long maxDay = now.minusDays(1).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);

        return LocalDate.ofEpochDay(randomDay);
    }

    public static User getRandomUser() {
        User user = new User();

        user.setEmail(getRandomEmail());
        user.setLogin(getRandomLogin());
        user.setName(getRandomUserName());
        user.setBirthday(getRandomPastDate());

        return user;
    }

    public static String getRandomFilmName() {
        return FAKER.text().text(1, 25);
    }

    public static String getRandomDescription() {
        return FAKER.text().text(0, 200);
    }

    public static LocalDate getRandomReleaseDate() {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        LocalDate maxDate = LocalDate.now();

        long minDay = minDate.toEpochDay();
        long maxDay = maxDate.toEpochDay();
        long randomDay = FAKER.random().nextLong(minDay, maxDay);

        return LocalDate.ofEpochDay(randomDay);
    }

    public static Long getRandomDuration() {
        return FAKER.random().nextLong(1L, 10_000L);
    }

    public static MpaRating getRandomMpa() {
        return new MpaRating(FAKER.random().nextLong(1L, 5L), null, null);
    }
}
