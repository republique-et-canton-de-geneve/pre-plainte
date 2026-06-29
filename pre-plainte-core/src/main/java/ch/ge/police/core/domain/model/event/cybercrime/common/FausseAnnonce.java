package ch.ge.police.core.domain.model.event.cybercrime.common;

import lombok.Data;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Détails pour un cybercrime de type "fausse annonce".
 */
@Data
public class FausseAnnonce {
  private String urlComplete;
  private String titreAnnonce;
  private String nomBailleur;
  private Boolean emailBailleurInconnu;
  private String emailBailleur;
  private Boolean telephoneBailleurInconnu;
  private String telephoneBailleur;
  private String adresseBienImmobilier;
  private Double montantDemande;
  private String modePaiementDemande;

  public void champsObligatoireCybercrime() {
    verifierChampsObligatoires();
    verifierLongueurs();
  }

  private void verifierChampsObligatoires() {
    verifierChampObligatoire(urlComplete, "L'URL de l’annonce est obligatoire.");
    verifierChampObligatoire(titreAnnonce, "Le titre de l’annonce est obligatoire.");
    verifierChampObligatoire(nomBailleur, "Le nom du bailleur est obligatoire.");
    verifierChampObligatoire(adresseBienImmobilier, "L'adresse du bien immobilier est obligatoire.");
    verifierChampObligatoire(montantDemande, "Le montant demandé est obligatoire.");
    verifierChampObligatoire(modePaiementDemande, "Le mode de paiement est obligatoire.");
  }

  private void verifierLongueurs() {
    verifierLongueurMax(titreAnnonce, TEXT_FIELD_MAX_LENGTH, "titreAnnonce");
    verifierLongueurMax(nomBailleur, TEXT_FIELD_MAX_LENGTH, "nomBailleur");

    verifierLongueurMax(urlComplete, TEXT_FIELD_MAX_LENGTH, "urlComplete");
    verifierLongueurMax(emailBailleur, TEXT_FIELD_MAX_LENGTH, "emailBailleur");
    verifierLongueurMax(telephoneBailleur, TEXT_FIELD_MAX_LENGTH, "telephoneBailleur");

    verifierLongueurMax(adresseBienImmobilier, TEXT_FIELD_MAX_LENGTH, "adresseBienImmobilier");
    verifierLongueurMax(modePaiementDemande, TEXT_FIELD_MAX_LENGTH, "modePaiementDemande");
  }
}
