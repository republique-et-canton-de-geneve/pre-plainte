package ch.ge.police.core.domain.model.event.dommagematerial;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
@Data
@EqualsAndHashCode(callSuper = true)
public class DommageMateriel extends IncidentBase {

  private TypeDommage typeDommage;
  private Double montantEstime;
  private String devise;
  private List<NatureDommage> naturesDommage;
  private List<ObjetIncident> objetDegrades;
  private String description;
  private Boolean constatPresent;
  private String dateConstat;

  @Override
  public TypeIncident getTypeIncident() {
    return TypeIncident.DOMMAGE;
  }

  @Override
  public void champsObligatoireIncident() {
    super.champsObligatoireIncident();

    validateurChampsObligatoires(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    validateurChampsObligatoires(getDateFinEvent(), "La date de fin d'événement est obligatoire.");

    verifier(typeDommage != null, "Le type de dommage doit être sélectionné.");
    verifier(!isBlank(devise), "La devise est obligatoire.");
    verifier(!isEmpty(naturesDommage), "Au moins une nature de dommage doit être sélectionnée.");
    verifier(!isBlank(description), "La description du dommage est obligatoire.");
    verifier(constatPresent != null, "L'indication de constat est obligatoire.");

    if (Boolean.TRUE.equals(constatPresent)) {
      verifier(!isBlank(dateConstat), "La date du constat est obligatoire si un constat est présent.");
    }
  }

  private void verifier(boolean condition, String messageErreur) {
    if (!condition) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  private boolean isEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
