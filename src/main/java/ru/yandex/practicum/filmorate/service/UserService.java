package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@Validated
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Validated(UpdateValidationGroup.class) @RequestBody User newUser) {
        if (!users.containsKey(newUser.getId()))
            throw new NotFoundException(String.format("User with id=%s not found", newUser.getId()));

        User oldUser = users.get(newUser.getId());
        if (nonNull(newUser.getEmail())) oldUser.setEmail(newUser.getEmail());
        if (nonNull(newUser.getLogin())) oldUser.setLogin(newUser.getLogin());
        if (!newUser.getName().equals(newUser.getLogin())) oldUser.setName(newUser.getName());
        if (nonNull(newUser.getBirthday())) oldUser.setBirthday(newUser.getBirthday());
        return oldUser;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }
}
