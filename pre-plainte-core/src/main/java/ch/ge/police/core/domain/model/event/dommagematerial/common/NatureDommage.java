package ch.ge.police.core.domain.model.event.dommagematerial.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum NatureDommage {

  DEGRADATIONS("degradations", "Dégradations"),
  TAGS_GRAFFITI("tags-graffiti", "Tags / graffitis"),
  AUTRE("autre", "Autre");

  private final String code;

  @Getter
  private final String label;

  NatureDommage(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static NatureDommage fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException("Nature de dommage invalide : " + code)
      );
  }
}
