package ru.yandex.practicum.filmorate.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;
import ru.yandex.practicum.filmorate.repository.impl.FeedDbStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedDbStorage feedDbStorage;

  public List<Event> getFeed(Long id) {
    return feedDbStorage.getFeed(id);
  }

  public void addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {

    Event event = new Event(
        Instant.now().toEpochMilli(),
        userId,
        eventType,
        operation,
        null,
        entityId
    );
    feedDbStorage.addEvent(event);
    log.info("Added event: {}", event);
  }
}