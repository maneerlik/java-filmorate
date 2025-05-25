package ru.yandex.practicum.filmorate.exception;

public class SqlParameterException extends RuntimeException {
    public SqlParameterException(String message) {
        super(message);
    }
}
