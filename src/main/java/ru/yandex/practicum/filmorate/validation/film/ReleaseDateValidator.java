package ru.yandex.practicum.filmorate.validation.film;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDate, LocalDate> {
    private LocalDate minReleaseDate;

    @Override
    public void initialize(ReleaseDate constraintAnnotation) {
        String minReleaseDateStr = constraintAnnotation.minReleaseDate();
        this.minReleaseDate = LocalDate.parse(minReleaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        return isNull(value) || value.isAfter(minReleaseDate);
    }
}
