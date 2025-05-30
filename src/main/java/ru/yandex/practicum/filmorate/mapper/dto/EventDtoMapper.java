package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

public class EventDtoMapper {

    public static EventDto mapToEventDto(Event event) {
        EventDto dto = new EventDto();
        dto.setTimestamp(event.getTimestamp());
        dto.setUserId(event.getUserId());
        dto.setEventType(event.getEventType());
        dto.setOperation(event.getOperation());
        dto.setEventId(event.getEventId());
        dto.setEntityId(event.getEntityId());
        return dto;
    }
}
