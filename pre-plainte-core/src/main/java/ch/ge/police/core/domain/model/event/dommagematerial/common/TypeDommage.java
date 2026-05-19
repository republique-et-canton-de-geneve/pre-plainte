package ch.ge.police.core.domain.model.event.dommagematerial.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum TypeDommage {

  DOMMAGE_PROPRIETE("dommage-propriete", "Dommage à une propriété"),
  DOMMAGE_VEHICULE("dommage-vehicule", "Dommage à un véhicule"),
  DOMMAGE_VELO("dommage-velo", "Dommage à un vélo"),
  AUTRE("autre", "Autre");

  private final String code;

  @Getter
  private final String label;

  TypeDommage(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static TypeDommage fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException("Type de dommage invalide : " + code)
      );
  }
}
