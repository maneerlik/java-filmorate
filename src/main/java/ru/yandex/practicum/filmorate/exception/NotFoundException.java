package ru.yandex.practicum.filmorate.exception;

/**
 * Кастомное исключение {@code NotFoundException}, бросаемое при
 * попытке обращения к несуществующей сущности
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
