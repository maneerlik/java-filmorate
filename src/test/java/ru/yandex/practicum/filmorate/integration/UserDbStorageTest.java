package ru.yandex.practicum.filmorate.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static ru.yandex.practicum.filmorate.util.TestDataGenerator.getRandomUser;

class UserDbStorageTest extends BaseDbStorageTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
    }


    @Test
    public void testCreateUser() {
        User createdUser = userStorage.createUser(user);

        assertThat(createdUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isNotNull();
                    assertThat(u.getEmail()).isEqualTo(email);
                    assertThat(u.getLogin()).isEqualTo(login);
                    assertThat(u.getName()).isEqualTo(userName);
                    assertThat(u.getBirthday()).isEqualTo(birthday);
                });
    }

    @Test
    public void testGetUser() {
        User createdUser = userStorage.createUser(user);
        long id = createdUser.getId();

        assertThat(userStorage.getUser(id))
                .isPresent()
                .hasValueSatisfying(foundUser -> {
                    assertThat(foundUser.getId()).isEqualTo(id);
                    assertThat(foundUser.getEmail()).isEqualTo(email);
                    assertThat(foundUser.getLogin()).isEqualTo(login);
                    assertThat(foundUser.getName()).isEqualTo(userName);
                    assertThat(foundUser.getBirthday()).isEqualTo(birthday);
                });
    }

    @Test
    public void testGetAllUser() {
        IntStream.range(0, 3).forEach(i -> userStorage.createUser(getRandomUser()));

        List<User> allUsers = new ArrayList<>(userStorage.getAllUsers());

        assertFalse(allUsers.isEmpty(), "users not found");
        assertEquals(3, allUsers.size(), "wrong number of users");
    }

    @Test
    public void testUpdateUser() {
        User createdUser = userStorage.createUser(user);
        long id = createdUser.getId();

        assertThat(userStorage.updateUser(createdUser))
                .isPresent()
                .hasValueSatisfying(updatedUser -> {
                    assertThat(updatedUser.getId()).isEqualTo(id);
                    assertThat(updatedUser.getEmail()).isEqualTo(email);
                    assertThat(updatedUser.getLogin()).isEqualTo(login);
                    assertThat(updatedUser.getName()).isEqualTo(userName);
                    assertThat(updatedUser.getBirthday()).isEqualTo(birthday);
                });
    }
}
