package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import lombok.Data;

/**
 * Représente une entreprise, une association ou une organisation.
 */
@Data
public class Organisation {

  private String nom;
  private String telephone;
  private String email;
  private Adresse adresse;


  public void validateurChampsObligatoire(Object value, String messageErreur){
    if (value == null) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof String valueStr && valueStr.isBlank()) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  public void validateOrganisationInfo() {
    validateurChampsObligatoire(nom, "Le nom est obligatoire.");
    validateurChampsObligatoire(adresse, "L'adresse est obligatoire.");
    validateurChampsObligatoire(email, "L'e-mail est obligatoire.");
    validateurChampsObligatoire(telephone, "Le téléphone est obligatoire.");
  }
}
