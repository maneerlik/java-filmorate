package ru.yandex.practicum.filmorate.repository;

import java.util.List;
import ru.yandex.practicum.filmorate.model.Event;

public interface FeedStorage {

  List<Event> getFeed(Long id);

  void addEvent(Event event);
}
