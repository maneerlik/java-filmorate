package ru.yandex.practicum.filmorate.repository.impl;

import java.sql.PreparedStatement;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.repository.FeedStorage;
import ru.yandex.practicum.filmorate.rowmapper.EventRowMapper;

@Slf4j
@Repository
public class FeedDbStorage extends BaseDbStorage implements FeedStorage {

    private static final String GET_FEED = """
            SELECT * FROM eventy
            WHERE user_id = ?;
            """;
    private static final String ADD_FEED = """
            INSERT INTO eventy (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?);
            """;
    protected final EventRowMapper mapper;

    public FeedDbStorage(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc);
        this.mapper = mapper;
    }

    public List<Event> getFeed(Long id) {
        return jdbc.query(GET_FEED, mapper, id);
    }

    public void addEvent(Event event) {
        long id = insert(ADD_FEED,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
        event.setEventId(id);
        log.debug("Added event: {}", event);
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new NotFoundException("Failed to save data");
        }
    }
}
