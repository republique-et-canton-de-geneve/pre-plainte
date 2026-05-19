package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum PlateformeUtilisee {

  ANIBIS("anibis", "Anibis"),
  FACEBOOK("facebook", "Facebook Marketplace"),
  RICARDO("ricardo", "Ricardo"),
  TUTTI("tutti", "Tutti"),
  AUTRE("autre", "Autre");

  private final String code;

  @Getter
  private final String label;

  PlateformeUtilisee(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static PlateformeUtilisee fromJson(String code) {
    return fromCode(code);
  }

  public static PlateformeUtilisee fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }

    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException("Plateforme utilisée inconnue : " + code)
      );
  }

}
