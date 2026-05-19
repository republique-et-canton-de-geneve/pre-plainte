package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import lombok.Data;

/**
 * Détails pour un cybercrime de type "commande frauduleuse".
 */
@Data
public class CommandeFrauduleuse {
  private String prestataire;
  private String dateDecouverte;
  private Double montant;
  private Boolean assurance;
  private Boolean emailCommandeInconnu;
  private String emailCommande;
  private Boolean telephoneCommandeInconnu;
  private String telephoneCommande;
  private Boolean livraisonAdresseLesee;
  private Adresse adresseLivraison;

  public void champsObligatoireCybercrime() {
    if (dateDecouverte == null || dateDecouverte.isBlank()) {
      throw new ValidationMetierException("La date de découverte est obligatoire.");
    }

    if (montant == null) {
      throw new ValidationMetierException("Le montant du délit est obligatoire.");
    }

    if (assurance == null) {
      throw new ValidationMetierException("L’information sur l’assurance est obligatoire.");
    }
  }
}
