package ru.yandex.practicum.filmorate.validation.film;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ReleaseDateValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDate {
    String message() default "Release date film is incorrect";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String minReleaseDate() default "1895-12-28";
}
