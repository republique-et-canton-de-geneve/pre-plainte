package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum TitreSejour {

  PERMIS_B("permis_b", "Permis B"),
  PERMIS_B_REFUGIE("permis_b_refugie", "Permis B (réfugié)"),
  PERMIS_C("permis_c", "Permis C"),
  PERMIS_CI("permis_ci", "Permis CI"),
  CARTE_LEGITIMATION("carte_legitimation", "Carte de légitimation"),
  PERMIS_L("permis_l", "Permis L"),
  PERMIS_F_REFUGIE("permis_f_refugie", "Permis F (réfugié)"),
  PERMIS_F_PROVISOIRE("permis_f_provisoire", "Permis F (provisoire)"),
  PERMIS_G("permis_g", "Permis G"),
  PERMIS_N("permis_n", "Permis N"),
  PERMIS_S("permis_s", "Permis S"),
  SANS_PERMIS("sans_permis", "Sans permis"),
  AUCUN("aucun", "Aucun");

  private final String code;

  @Getter
  private final String label;

  TitreSejour(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  @JsonCreator
  public static TitreSejour fromJson(String code) {
    return fromCode(code);
  }

  public static TitreSejour fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }

    return Arrays.stream(values())
      .filter(v -> v.code.equals(code))
      .findFirst()
      .orElseThrow(() ->
        new ValidationMetierException(
          "Titre de séjour invalide : " + code
        )
      );
  }
}
