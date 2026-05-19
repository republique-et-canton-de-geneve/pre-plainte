package ch.ge.police.core.port.out;

import ch.ge.police.core.domain.model.PrePlainte;

public interface PrePlainteSubmissionPort {
  /** Transforme la pré-plainte en document XML eCH-0051 et la dépose sur le NAS */
  void soumettrePrePlainteVersNas(PrePlainte prePlainte);
}
