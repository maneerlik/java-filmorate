package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long eventId;
    private Long entityId;
}
