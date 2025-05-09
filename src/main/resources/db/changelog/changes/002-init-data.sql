-- liquibase formatted sql

-- changeset smirnovs:002-init-data
INSERT INTO genres (name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');

INSERT INTO mpa_ratings (name, description) VALUES
    ('G', 'General Audiences – нет возрастных ограничений'),
    ('PG', 'Parental Guidance Suggested – детям рекомендуется смотреть с родителями'),
    ('PG-13', 'Parents Strongly Cautioned – родителям не рекомендуется показывать детям до 13 лет'),
    ('R', 'Restricted – требуется присутствие взрослого при просмотре для лиц младше 17 лет'),
    ('NC-17', 'No One 17 and Under Admitted – запрещено для лиц моложе 17 лет');