package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@Validated
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }


    public Director createDirector(@Valid @RequestBody Director director) {
        Director createDirector = directorStorage.createDirector(director);
        log.info("Director created: {}", createDirector);
        return createDirector;
    }

    public Director getDirector(Long idDirector) {
        return directorStorage.getDirector(idDirector)
                .orElseThrow(() -> new NotFoundException("Director not found"));
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director updateDirector(@Valid @RequestBody Director director) {
        Optional<Director> updatedDirector = directorStorage.updateDirector(director);
        if (updatedDirector.isEmpty())
            throw new NotFoundException(String.format("Director with id=%s not found", director.getId()));
        log.info("Director updated: {}", updatedDirector);
        return updatedDirector.get();
    }

    public void removeDirector(Long idDirector) {
        directorStorage.removeDirector(idDirector)
                .orElseThrow(() -> new NotFoundException("Director not found"));
        log.info("Removed director with id = {}", idDirector);
    }
}
