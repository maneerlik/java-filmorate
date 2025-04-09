package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public User create(@RequestBody User user) {
        User createdUser = userService.create(user);
        log.info("User created: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        User userUpdated = userService.update(newUser);
        log.info("User updated: {}", userUpdated);
        return userUpdated;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }
}
