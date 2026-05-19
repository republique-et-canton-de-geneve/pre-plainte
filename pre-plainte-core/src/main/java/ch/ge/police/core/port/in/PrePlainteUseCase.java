package ch.ge.police.core.port.in;

import ch.ge.police.core.domain.model.PrePlainte;

public interface PrePlainteUseCase {
  /** Récupère le brouillon d’un utilisateur */
  PrePlainte recupererBrouillon(String demandeId);

  /** Enregistre ou met à jour la pré-plainte en mode brouillon */
  String enregistrerPrePlainteEnModeBrouillon(PrePlainte prePlainte);

  /** Soumet définitivement la pré-plainte (XML + NAS) */
  String soumettrePrePlainte(PrePlainte prePlainte);
}
