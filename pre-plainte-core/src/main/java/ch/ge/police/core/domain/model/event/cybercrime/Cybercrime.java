package ch.ge.police.core.domain.model.event.cybercrime;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.fichier.Fichier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Locale;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXTAREA_MAX_LENGTH;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Représente un incident de type Cybercrime.
 * Peut être une fausse annonce, un achat non reçu ou une commande frauduleuse.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Cybercrime extends IncidentBase {
  private TypeCybercrime typeCybercrime;
  private String descriptionCybercrime;
  private String datePremierContact;
  private String dateDernierContact;
  private List<Fichier> fichiersCybercrime;

  private CommandeFrauduleuse commandeFrauduleuse;
  private AchatNonRecu achatNonRecu;
  private FausseAnnonce fausseAnnonce;

  @Override
  public TypeIncident getTypeIncident() {
    return TypeIncident.CYBER;
  }

  @Override
  public void champsObligatoireIncident() {
    verifierChampsObligatoires();
    verifierLongueurs();

    switch (typeCybercrime.getCode().toLowerCase(Locale.ROOT)) {
      case "fausse-annonce":
        verifierDatesContact();
        verifierSousType(fausseAnnonce, "fausse annonce");
        break;
      case "achat-non-recu":
        verifierDatesContact();
        verifierSousType(achatNonRecu, "achat non reçu");
        break;
      case "commande-frauduleuse":
        verifierDatesEvenement();
        verifierSousType(commandeFrauduleuse, "commande frauduleuse");
        break;
      case "cyberharcelement", "rancongiciel", "autre":
        // Ces types n'ont pas de sous-objets spécifiques, validation basique seulement
        break;
      default:
        throw new ValidationMetierException("Type de cybercrime non reconnu: " + typeCybercrime);
    }
  }

  @Override
  protected void verifierChampsObligatoires() {
    super.verifierChampsObligatoires();

    verifierChampObligatoire(typeCybercrime, "Le type de cybercrime est obligatoire.");
    verifierChampObligatoire(descriptionCybercrime, "La description du cybercrime est obligatoire.");
  }

  @Override
  protected void verifierLongueurs() {
    super.verifierLongueurs();

    verifierLongueurMax(descriptionCybercrime, TEXTAREA_MAX_LENGTH, "descriptionCybercrime");
    verifierLongueurMax(datePremierContact, TEXT_FIELD_MAX_LENGTH, "datePremierContact");
    verifierLongueurMax(dateDernierContact, TEXT_FIELD_MAX_LENGTH, "dateDernierContact");
  }

  private void verifierDatesContact() {
    verifierChampObligatoire(datePremierContact, "La date de premier contact est obligatoire.");
    verifierChampObligatoire(dateDernierContact, "La date de dernier contact est obligatoire.");
  }

  private void verifierDatesEvenement() {
    verifierChampObligatoire(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    verifierChampObligatoire(getDateFinEvent(), "La date de fin d'événement est obligatoire.");
  }

  private void verifierSousType(Object sousType, String nomSousType) {
    verifierChampObligatoire(sousType, "Le sous-type " + nomSousType + " doit être renseigné.");

    if (sousType instanceof FausseAnnonce f) {
      f.champsObligatoireCybercrime();
    }
    if (sousType instanceof AchatNonRecu a) {
      a.champsObligatoireCybercrime();
    }
    if (sousType instanceof CommandeFrauduleuse c) {
      c.champsObligatoireCybercrime();
    }
  }
}
