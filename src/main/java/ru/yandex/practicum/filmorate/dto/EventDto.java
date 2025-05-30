package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;

@Data
public class EventDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long eventId;
    private Long entityId;
}
