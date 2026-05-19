package ch.ge.police.core.domain.model;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.rendezvous.CreneauRendezVous;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une pré-plainte complète.
 * Contient :
 *  - l'id de la demande
 *  - les informations personnelles du déclarant,
 *  - les informations de l’incident
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePlainte {

  private String demandeId;
  private InformationsPersonnelles informationsPersonnelles;
  private Incident incident;
  private CreneauRendezVous creneauRendezVous;
  private Fichier pdfRecapitulatif;

  public PrePlainte(String demandeId, InformationsPersonnelles informationsPersonnelles, Incident incident) {
    this.demandeId = demandeId;
    this.informationsPersonnelles = informationsPersonnelles;
    this.incident = incident;
  }

  public void validateChampsPresPlainte() {
    if (demandeId == null) {
      throw new ValidationMetierException("L'id de demande est obligatoire.");
    }

    if (informationsPersonnelles == null) {
      throw new ValidationMetierException("Les informations personnelles sont obligatoires.");
    }
    informationsPersonnelles.validate();

    if (incident == null) {
      throw new ValidationMetierException("Un incident doit être renseigné.");
    }
    incident.validate();

    if (informationsPersonnelles.hasTiers() && informationsPersonnelles.getTiers() == null) {
      throw new ValidationMetierException("Les informations du tiers sont manquantes alors qu’un lien avec un tiers est indiqué.");
    }

    if (informationsPersonnelles.hasOrganisation() && informationsPersonnelles.getOrganisation() == null) {
      throw new ValidationMetierException("Les informations de l’organisation sont manquantes alors qu’un lien avec une entreprise est indiqué.");
    }
  }
}
