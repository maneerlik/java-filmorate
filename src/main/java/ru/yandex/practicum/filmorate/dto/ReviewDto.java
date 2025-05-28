package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

  private Long id;
  private String content;
  private Boolean isPositive;
  private Long userId;
  private Long filmId;
  private Integer useful;
}
