package ru.yandex.practicum.filmorate.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FilmDbStorageTest extends BaseDbStorageTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
    }


    @Test
    public void testCreateFilm() {
        Film createdFilm = filmStorage.createFilm(film);

        assertThat(createdFilm)
                .isNotNull()
                .satisfies(f -> {
                    assertThat(f.getId()).isNotNull();
                    assertThat(f.getName()).isEqualTo(filmName);
                    assertThat(f.getDescription()).isEqualTo(description);
                    assertThat(f.getReleaseDate()).isEqualTo(releaseDate);
                    assertThat(f.getDuration()).isEqualTo(duration);
                    assertThat(f.getMpa()).isEqualTo(mpa);
                });
    }
}
