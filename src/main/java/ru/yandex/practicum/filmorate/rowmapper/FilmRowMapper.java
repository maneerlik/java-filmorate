package ru.yandex.practicum.filmorate.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<FilmDto> {

    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        Set<GenreDto> genres = new HashSet<>();
        Set<Long> likes = new HashSet<>();

        // жанры и лайки будут загружены и добавлены в filmDto отдельным запросом
        return FilmDto.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .mpa(MpaRatingDto.builder()
                        .id(rs.getLong("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .description(rs.getString("mpa_description"))
                        .build())
                .genres(genres)
                .likes(likes)
                .build();
    }
}
