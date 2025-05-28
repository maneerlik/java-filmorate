package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

  @NotNull(message = "Id cannot be empty", groups = UpdateValidationGroup.class)
  private Long reviewId;

  private String content;

  @NotNull(message = "Sign isPositive cannot be empty", groups = Default.class)
  private Boolean isPositive;

  @NotNull(message = "userId cannot be empty", groups = Default.class)
  private Long userId;

  @NotNull(message = "filmId cannot be empty", groups = Default.class)
  private Long filmId;

  private Integer useful;
}
