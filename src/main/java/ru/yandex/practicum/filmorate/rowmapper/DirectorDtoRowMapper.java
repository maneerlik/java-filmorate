package ru.yandex.practicum.filmorate.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.dto.DirectorDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectorDtoRowMapper implements RowMapper<DirectorDto> {

    @Override
    public DirectorDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return DirectorDto.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}


