package ru.yandex.practicum.filmorate;

import jakarta.validation.groups.Default;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest extends BaseTest {

    @InjectMocks
    private FilmController controller;
    private Film film;
    
    @BeforeEach
    public void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Interstellar");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2014, 11,7));
        film.setDuration(169L);
    }


    @Test
    void testCreateFilm() {
        Film createdFilm = controller.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Interstellar", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2014, 11, 7), createdFilm.getReleaseDate());
        assertEquals(169L, createdFilm.getDuration());
    }

    @Test
    void testUpdateFilm() {
        Film createdFilm = controller.create(film);
        Film newFilm = new Film();
        newFilm.setId(createdFilm.getId());
        newFilm.setDescription("Sci-Fi epic directed by Christopher Nolan");

        Film updatedFilm = controller.update(newFilm);

        assertEquals("Sci-Fi epic directed by Christopher Nolan", updatedFilm.getDescription());
        assertEquals("Interstellar", createdFilm.getName());
        assertEquals(LocalDate.of(2014, 11, 7), createdFilm.getReleaseDate());
        assertEquals(169L, createdFilm.getDuration());
    }

    @Test
    void testGetAllFilms() {
        controller.create(film);
        controller.create(film);
        controller.create(film);

        Collection<Film> films = controller.findAll();
        List<String> names = films.stream().map(Film::getName).toList();

        assertEquals(3, films.size());
        assertTrue(names.stream().allMatch(name -> name.equals(film.getName())));
    }

    @Test
    void testUpdateFilmNotFound() {
        film.setId(Long.MAX_VALUE);
        assertThrows(NotFoundException.class, () -> controller.update(film));
    }

    @Test
    void testNullNameValidation() {
        film.setName(null);

        List<String> violations = getViolationsList(film);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Name cannot be empty"));
    }

    @Test
    void testEmptyNameValidation() {
        film.setName("");

        List<String> violations = getViolationsList(film, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Name cannot be empty"));
    }

    @Test
    void testBlankNameValidation() {
        film.setName("\s\s\s");

        List<String> violations = getViolationsList(film);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Name cannot be empty"));
    }

    @Test
    void testOverMaxLimitCharacterDescriptionValidation() {
        film.setDescription("""
                "Interstellar," directed by Christopher Nolan, is a sci-fi epic that follows a team of astronauts\s
                traveling through a wormhole near Saturn in search of a new home for humanity. As Earth faces\s
                ecological collapse, former pilot Cooper must leave his family behind to explore distant planets and\s
                ensure the survival of the human race, grappling with time, and sacrifice across vast cosmic landscapes.
                """);

        List<String> violations = getViolationsList(film, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Max description length is 200 characters"));
    }

    @Test
    void testReleaseDateValidation() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        List<String> violations = getViolationsList(film, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Minimal release date is 1895-12-28"));
    }

    @Test
    void testNegativeDurationValidation() {
        film.setDuration(-1L);

        List<String> violations = getViolationsList(film, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Film duration must be a positive number"));
    }

    @Test
    void testNullIdValidation() {
        film.setId(null);

        List<String> violations = getViolationsList(film, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Id cannot be empty"));
    }
}
