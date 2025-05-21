package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс {@code User} - модель пользователя
 *
 * <p>Аннотация {@code @Data} автоматически генерирует геттеры, сеттеры, методы {@code equals()},
 * {@code hashCode()}, а также {@code toString()}. Аннотация {@code @NoArgsConstructor} автоматически генерирует
 * конструктор по умолчанию, который необходим при сериализации/десериализации объектов JSON</p>
 *
 * <p>Поля класса:</p>
 * <ul>
 *   <li>{@code id} - уникальный идентификатор пользователя. Может быть {@code null}</li>
 *   <li>{@code email} - электронная почта. Не может быть пустой и должна содержать символ {@code @}</li>
 *   <li>{@code login} - логин пользователя. Не может быть пустым и содержать пробелы</li>
 *   <li>{@code name} - имя пользователя. Может быть {@code null} (в данном случае name == login)</li>
 *   <li>{@code birthday} - дата рождения. {@link LocalDate} не может быть в будущем</li>
 * </ul>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @NotNull(message = "Id cannot be empty", groups = UpdateValidationGroup.class)
    private Long id;

    @NotBlank(message = "Email cannot be empty", groups = Default.class)
    @Email(
            message = "Email must contain '@' and a valid domain name",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private String email;

    @NotBlank(message = "Login cannot be empty", groups = Default.class)
    @Pattern(
            regexp = "\\S+", message = "Login must not contain spaces",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private String login;

    private String name;

    @Past(
            message = "Birthday cannot be in the future",
            groups = {Default.class, UpdateValidationGroup.class}
    )
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();


    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }
}
