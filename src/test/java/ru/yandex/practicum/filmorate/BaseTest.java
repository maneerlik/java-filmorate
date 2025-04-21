package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.List;
import java.util.Set;

public abstract class BaseTest {

    // TODO: уменьшить дублирование кода в тестах контроллеров

    /**
     * Возвращает список сообщений об ошибках валидации для указанного объекта {@link Object}
     *
     * <p>Метод выполняет валидацию объекта {@code entity} с использованием указанных групп валидации.
     * Если валидация выявляет нарушения, метод возвращает список сообщений об ошибках.
     * Если нарушений нет, возвращается пустой список</p>
     *
     * @param entity  объект {@link Object}, который требуется проверить на соответствие ограничениям валидации
     * @param groups  варарг классов, представляющих группы валидации. Если группы не указаны, используется группа
     *                по умолчанию ({@link jakarta.validation.groups.Default})
     * @return        список строк, содержащих сообщения об ошибках валидации или пустой в противном случае
     */
    protected List<String> getViolationsList(Object entity, Class<?>... groups) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(entity, groups);

            return violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
        }
    }
}
