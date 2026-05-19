package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum LienAvecPersonne {
  MOI_MEME, TIERS, ENTREPRISE;

  @JsonValue
  public String jsonValue() {
    return name();
  }

  @JsonCreator
  public static LienAvecPersonne fromValue(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    try {
      return LienAvecPersonne.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      throw new ValidationMetierException("Valeur inconnue pour lienAvecPersonne : " + value);
    }
  }
}
