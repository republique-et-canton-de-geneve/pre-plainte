package ch.ge.police.core.port.out;

import ch.ge.police.core.domain.model.PrePlainte;

public interface PrePlainteBrouillontPort {

  void sauvegarderBrouillon(PrePlainte prePlainte, String demandeId);

  PrePlainte chargerBrouillon(String demandeId);
}
