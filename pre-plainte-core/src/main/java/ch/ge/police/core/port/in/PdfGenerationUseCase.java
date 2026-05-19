package ch.ge.police.core.port.in;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.fichier.Fichier;

public interface PdfGenerationUseCase {

  /**
   * Génère un PDF simple avec un message texte.
   * @param dto à mettre dans le PDF
   * @return le PDF sous forme de tableau de bytes
   */
  byte[] generatePdf(PrePlainte dto);

  /**
   * Génère un fichier PDF récapitulatif à partir des données de la préplainte.
   * @param dto les données à inclure dans le PDF
   * @return le fichier PDF sous forme de {@link Fichier}
   */
  Fichier generateFilePdf(PrePlainte dto);
}
