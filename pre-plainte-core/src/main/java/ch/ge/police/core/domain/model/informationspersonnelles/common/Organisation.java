package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import lombok.Data;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Représente une entreprise, une association ou une organisation.
 */
@Data
public class Organisation {

  private String nom;
  private String telephone;
  private String email;
  private Adresse adresse;

  public void validateOrganisationInfo() {
    verifierChampsObligatoires();
    verifierLongueurs();

    if (adresse != null) {
      adresse.validate();
    }
  }

  private void verifierChampsObligatoires() {
    verifierChampObligatoire(nom, "Le nom est obligatoire.");
    verifierChampObligatoire(adresse, "L'adresse est obligatoire.");
    verifierChampObligatoire(email, "L'e-mail est obligatoire.");
    verifierChampObligatoire(telephone, "Le téléphone est obligatoire.");
  }

  private void verifierLongueurs() {
    verifierLongueurMax(nom, TEXT_FIELD_MAX_LENGTH, "nom");
    verifierLongueurMax(email, TEXT_FIELD_MAX_LENGTH, "email");
    verifierLongueurMax(telephone, TEXT_FIELD_MAX_LENGTH, "telephone");
  }
}
