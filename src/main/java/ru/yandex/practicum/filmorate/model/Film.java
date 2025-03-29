package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;
import ru.yandex.practicum.filmorate.validation.film.ReleaseDate;

import java.time.LocalDate;

/**
 * Класс {@code Film} - модель фильма
 *
 * <p>Аннотация {@code @Data} автоматически генерирует геттеры, сеттеры, методы {@code equals()},
 * {@code hashCode()}, а также {@code toString()}. Аннотация {@code @NoArgsConstructor} автоматически генерирует
 * конструктор по умолчанию, который необходим при сериализации/десериализации объектов JSON</p>
 *
 * <p>Поля класса:</p>
 * <ul>
 *   <li>{@code id} - уникальный идентификатор фильма. Может быть {@code null}</li>
 *   <li>{@code name} - название фильма. Не может быть пустым</li>
 *   <li>{@code description} - описание фильма. Не должно превышать 200 символов</li>
 *   <li>{@code releaseDate} - дата релиза фильма. {@link LocalDate}</li>
 *   <li>{@code duration} - продолжительность фильма. {@link Long}. Не может быть отрицательным</li>
 * </ul>
 */

@Data
@NoArgsConstructor
public class Film {

    @NotNull(message = "Id cannot be empty", groups = UpdateValidationGroup.class)
    private Long id;

    @NotBlank(message = "Name cannot be empty", groups = Default.class)
    private String name;

    @Size(
            max = 200,
            message = "Max description length is 200 characters",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private String description;

    @ReleaseDate(
            message = "Minimal release date is 1895-12-28",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private LocalDate releaseDate;

    @Positive(
            message = "Film duration must be a positive number",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private Long duration;
}
