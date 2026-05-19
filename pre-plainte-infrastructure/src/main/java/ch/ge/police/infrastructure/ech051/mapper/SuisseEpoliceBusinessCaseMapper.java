package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Attachment;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Mapper dédié à la construction du BusinessCase (dossier)
 * avec les fichiers attachés au format eCH-0051.
 */
@Component
public class SuisseEpoliceBusinessCaseMapper {

  /**
   * Construit le BusinessCase avec tous les fichiers attachés.
   */
  public BusinessCase buildBusinessCase(PrePlainte prePlainte, DeclarationType declarationType) {
    List<Attachment> attachments = Stream.of(
        Optional.ofNullable(prePlainte.getIncident())
          .map(Incident::getDetails)
          .map(IncidentBase::getFichiers)
          .orElse(null),

        Optional.ofNullable(prePlainte.getIncident())
          .map(Incident::getDetails)
          .filter(Cybercrime.class::isInstance)
          .map(Cybercrime.class::cast)
          .map(Cybercrime::getFichiersCybercrime)
          .orElse(null),

        Optional.ofNullable(prePlainte.getPdfRecapitulatif())
          .map(List::of)
          .orElse(null)
      )
      .filter(Objects::nonNull)
      .flatMap(Collection::stream)
      .map(this::buildAttachment)
      .toList();

    Ech0051DocumentPayload.File file = Ech0051DocumentPayload.File.builder()
      .attachment(attachments)
      .build();

    return BusinessCase.builder()
      .key(resolveBusinessCaseKey(declarationType))
      .caseNumber(prePlainte.getDemandeId() != null ? prePlainte.getDemandeId() : "PPL")
      .file(List.of(file))
      .build();
  }

  /**
   * Détermine la clé du BusinessCase selon le type de déclaration.
   */
  private String resolveBusinessCaseKey(DeclarationType declarationType) {
    return switch (declarationType) {
      case TIERS -> Ech051Constants.BUSINESS_CASE_KEY_TIERS;
      case ENTREPRISE -> Ech051Constants.BUSINESS_CASE_KEY_ENTREPRISE;
      default -> Ech051Constants.BUSINESS_CASE_KEY;
    };
  }

  /**
   * Transforme un fichier métier en pièce jointe eCH-0051.
   */
  private Attachment buildAttachment(Fichier fichier) {
    return Attachment.builder()
      .filename(fichier.nom())
      .content(fichier.contenuBase64())
      .build();
  }
}
