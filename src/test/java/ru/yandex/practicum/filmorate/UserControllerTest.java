package ru.yandex.practicum.filmorate;

import jakarta.validation.groups.Default;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.repository.impl.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends BaseTest {

    private UserController controller;
    private User user;

    @BeforeEach
    public void setUp() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        controller = new UserController(service);

        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }


    @Test
    void testCreateUser() {
        User createdUser = controller.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@gmail.com", createdUser.getEmail());
        assertEquals("login", createdUser.getLogin());
        assertEquals("Name", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    void testUpdateUser() {
        User createdUser = controller.create(user);
        User newUser = new User();
        newUser.setId(createdUser.getId());
        newUser.setEmail("updated@gmail.com");
        newUser.setLogin("updatedlogin");

        User updatedUser = controller.update(newUser);

        assertEquals("updated@gmail.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Name", updatedUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void testGetAllUsers() {
        controller.create(user);
        controller.create(user);
        controller.create(user);

        Collection<User> users = controller.findAll();
        List<String> names = users.stream().map(User::getName).toList();

        assertEquals(3, names.size());
        assertTrue(names.stream().allMatch(name -> name.equals(user.getName())));
    }

    @Test
    void testUpdateUserNotFound() {
        user.setId(Long.MAX_VALUE);
        assertThrows(NotFoundException.class, () -> controller.update(user));
    }

    @Test
    void testNullEmailValidation() {
        user.setEmail(null);

        List<String> violations = getViolationsList(user);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Email cannot be empty"));
    }

    @Test
    void testEmptyEmailValidation() {
        user.setEmail("");

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Email cannot be empty"));
    }

    @Test
    void testBlankEmailValidation() {
        user.setEmail("\s\s\s");

        List<String> violations = getViolationsList(user);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Email cannot be empty"));
    }

    @Test
    void testInvalidEmailValidation() {
        user.setEmail("invalid_mail.com");

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Email must contain '@' and a valid domain name"));
    }

    @Test
    void testNullLoginValidation() {
        user.setLogin(null);

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Login cannot be empty"));
    }

    @Test
    void testEmptyLoginValidation() {
        user.setLogin("");

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Login cannot be empty"));
        assertTrue(violations.contains("Login must not contain spaces"));
    }

    @Test
    void testBlankLoginValidation() {
        user.setLogin("\s\s\s");

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Login cannot be empty"));
        assertTrue(violations.contains("Login must not contain spaces"));
    }

    @Test
    void testPresenceOfSpacesLoginValidation() {
        user.setLogin("test spaces login name");

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Login must not contain spaces"));
    }

    @Test
    void testFutureBirthdayValidation() {
        user.setBirthday(LocalDate.of(2999, 1, 1));

        List<String> violations = getViolationsList(user, Default.class, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Birthday cannot be in the future"));
    }

    @Test
    void testNullIdValidation() {
        user.setId(null);

        List<String> violations = getViolationsList(user, UpdateValidationGroup.class);

        assertFalse(violations.isEmpty(), "Violations empty");
        assertTrue(violations.contains("Id cannot be empty"));
    }
}
