package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum TypeDocumentIdentite {
  CARTE_IDENTITE("carte_identite", "Carte d'identité"),
  PASSEPORT("passeport", "Passeport"),
  DOCUMENTS_VOLES_PERDUS("documents_voles_perdus", "Documents volés ou perdus");

  private final String code;

  @Getter
  private final String label;

  TypeDocumentIdentite(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static TypeDocumentIdentite fromJson(String code) {
    return fromCode(code);
  }

  public static TypeDocumentIdentite fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }

    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException(
          "Titre de document d'identité invalide : " + code
        )
      );
  }
}
