package ru.yandex.practicum.filmorate.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {

  @Override
  public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
    Event event = new Event();
    event.setTimestamp(rs.getLong("timestamp"));
    event.setUserId(rs.getLong("user_id"));
    event.setEventType(EventType.valueOf(rs.getString("event_type")));
    event.setOperation(EventOperation.valueOf(rs.getString("operation")));
    event.setEventId(rs.getLong("event_id"));
    event.setEntityId(rs.getLong("entity_id"));
    return event;
  }
}