package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import lombok.Data;

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
    if (titreAnnonce == null || titreAnnonce.isBlank()) {
      throw new ValidationMetierException("Le titre de l’annonce est obligatoire.");
    }

    if (nomBailleur == null || nomBailleur.isBlank()) {
      throw new ValidationMetierException("Le nom du bailleur est obligatoire.");
    }

    if (montantDemande == null) {
      throw new ValidationMetierException("Le montant demandé est obligatoire.");
    }

    if (modePaiementDemande == null || modePaiementDemande.isBlank()) {
      throw new ValidationMetierException("Le mode de paiement est obligatoire.");
    }
  }
}
