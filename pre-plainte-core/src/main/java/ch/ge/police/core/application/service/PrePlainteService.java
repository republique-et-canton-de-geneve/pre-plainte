package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.port.in.DemandeIdUseCase;
import ch.ge.police.core.port.in.PdfGenerationUseCase;
import ch.ge.police.core.port.in.PrePlainteUseCase;
import ch.ge.police.core.port.out.PrePlainteBrouillontPort;
import ch.ge.police.core.port.out.PrePlainteSubmissionPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class PrePlainteService implements PrePlainteUseCase {

  private final PrePlainteBrouillontPort presPlainteBrouillon;
  private final PrePlainteSubmissionPort presPlainteSubmission;
  private final DemandeIdUseCase demandeIdUseCase;
  private final PdfGenerationUseCase pdfGenerationUseCase;

  @Override
  public String soumettrePrePlainte(PrePlainte prePlainte) {
    String demandeId = getDemandeId(prePlainte);
    prePlainte.setDemandeId(demandeId);
    prePlainte.validateChampsPresPlainte();

    Fichier pdf = pdfGenerationUseCase.generateFilePdf(prePlainte);
    prePlainte.setPdfRecapitulatif(pdf);

    presPlainteSubmission.soumettrePrePlainteVersNas(prePlainte);
    return demandeId;
  }

  @Override
  public PrePlainte recupererBrouillon(String demandeId) {
    return presPlainteBrouillon.chargerBrouillon(demandeId);
  }

  @Override
  public String enregistrerPrePlainteEnModeBrouillon(PrePlainte prePlainte) {
    String demandeId = getDemandeId(prePlainte);
    prePlainte.setDemandeId(demandeId);
    presPlainteBrouillon.sauvegarderBrouillon(prePlainte, demandeId);
    return demandeId;
  }

  private String getDemandeId(PrePlainte prePlainte) {
    String demandeId = prePlainte.getDemandeId();
    if (demandeId == null || demandeId.isBlank()) {
      TypeIncident typeIncident = prePlainte.getIncident() != null ? prePlainte.getIncident().getTypeIncident() : null;
      demandeId = demandeIdUseCase.generate(typeIncident).value();
    }
    return demandeId;
  }
}
