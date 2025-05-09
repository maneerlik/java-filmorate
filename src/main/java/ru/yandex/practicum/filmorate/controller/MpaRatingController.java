package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    public MpaRatingController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }


    @GetMapping("/{id}")
    public MpaRating getMpaRating(@PathVariable Long id) {
        return mpaRatingService.getMpaRating(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
    }

    @GetMapping
    public Collection<MpaRating> getAllMpaRatings() {
        return mpaRatingService.findAll();
    }
}
