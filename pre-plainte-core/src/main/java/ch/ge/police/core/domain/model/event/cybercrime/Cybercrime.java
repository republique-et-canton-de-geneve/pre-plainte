package ch.ge.police.core.domain.model.event.cybercrime;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.fichier.Fichier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Locale;

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
    verifierNonVide(typeCybercrime != null ? typeCybercrime.getCode() : null, "Le type de cybercrime est obligatoire.");
    verifierNonVide(descriptionCybercrime, "La description du cybercrime est obligatoire.");

    // Validation selon le type de cybercrime
    switch (typeCybercrime.getCode().toLowerCase(Locale.ROOT)) {
      case "fausse-annonce":
        verifierDatesContact();
        verifierEtValider(fausseAnnonce, "fausse annonce");
        break;
      case "achat-non-recu":
        verifierDatesContact();
        verifierEtValider(achatNonRecu, "achat non reçu");
        break;
      case "commande-frauduleuse":
        verifierDatesEvenement();
        verifierEtValider(commandeFrauduleuse, "commande frauduleuse");
        break;
      case "cyberharcelement", "rancongiciel", "autre":
        // Ces types n'ont pas de sous-objets spécifiques, validation basique seulement
        break;
      default:
        throw new ValidationMetierException("Type de cybercrime non reconnu: " + typeCybercrime);
    }
  }

  private void verifierDatesContact() {
    verifierNonVide(datePremierContact, "La date de premier contact est obligatoire.");
    verifierNonVide(dateDernierContact, "La date de dernier contact est obligatoire.");
  }

  private void verifierDatesEvenement() {
    verifierNonVide(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    verifierNonVide(getDateFinEvent(), "La date de fin d'événement est obligatoire.");
  }

  private void verifierNonVide(String champ, String messageErreur) {
    if (champ == null || champ.isBlank()) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  private void verifierEtValider(Object sousType, String nomSousType) {
    if (sousType == null) {
      throw new ValidationMetierException("Le sous-type " + nomSousType + " doit être renseigné.");
    }
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
