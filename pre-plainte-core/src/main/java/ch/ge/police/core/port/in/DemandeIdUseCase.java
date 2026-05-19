package ch.ge.police.core.port.in;

import ch.ge.police.core.domain.model.demande.DemandeId;
import ch.ge.police.core.domain.model.event.common.TypeIncident;

public interface DemandeIdUseCase {
  DemandeId generate(TypeIncident typeIncident);
}
