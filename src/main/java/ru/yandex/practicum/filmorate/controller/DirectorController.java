package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        return directorService.getDirector(id);
    }

    @GetMapping
    public Collection<Director> findAll() {
        return directorService.getAllDirectors();
    }

    @PutMapping
    public Director update(@RequestBody Director newDirector) {
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        directorService.removeDirector(id);
    }
}

