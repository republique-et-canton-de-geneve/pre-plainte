package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum TypeCybercrime {

  COMMANDE_FRAUDULEUSE("commande-frauduleuse", "Commande frauduleuse"),
  ACHAT_NON_RECU("achat-non-recu", "Achat non reçu"),
  FAUSSE_ANNONCE("fausse-annonce", "Fausse annonce"),
  CYBERHARCELEMENT("cyberharcelement", "Cyberharcèlement"),
  RANCONGICIEL("rancongiciel", "Rançongiciel"),
  AUTRE("autre", "Autre");

  private final String code;

  @Getter
  private final String label;

  TypeCybercrime(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static TypeCybercrime fromJson(String code) {
    return fromCode(code);
  }

  public static TypeCybercrime fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }

    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException("Type cybercrime inconnu : " + code)
      );
  }
}
