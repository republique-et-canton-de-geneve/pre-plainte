package ch.ge.police.core.domain.model.rendezvous;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

public record CreneauRendezVous(
  String id,
  String date,
  String heureDebut,
  String heureFin,
  String lieu,
  String codeRdv
) {
  public void validate() {
    verifierLongueurMax(id, TEXT_FIELD_MAX_LENGTH, "id");
    verifierLongueurMax(date, TEXT_FIELD_MAX_LENGTH, "date");
    verifierLongueurMax(heureDebut, TEXT_FIELD_MAX_LENGTH, "heureDebut");
    verifierLongueurMax(heureFin, TEXT_FIELD_MAX_LENGTH, "heureFin");
    verifierLongueurMax(lieu, TEXT_FIELD_MAX_LENGTH, "lieu");
    verifierLongueurMax(codeRdv, TEXT_FIELD_MAX_LENGTH, "codeRdv");
  }
}
