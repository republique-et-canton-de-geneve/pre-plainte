package ch.ge.police.core.domain.model.event.dommagematerial;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierCollectionNonVide;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXTAREA_MAX_LENGTH;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

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

    verifierChampsObligatoires();

    if (objetDegrades != null && !objetDegrades.isEmpty()) {
      objetDegrades.forEach(ObjetIncident::champsObligatoire);
    }

    verifierLongueurs();
  }

  @Override
  protected void verifierChampsObligatoires() {
    super.verifierChampsObligatoires();

    verifierChampObligatoire(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    verifierChampObligatoire(getDateFinEvent(), "La date de fin d'événement est obligatoire.");

    verifierChampObligatoire(typeDommage, "Le type de dommage doit être sélectionné.");
    verifierCollectionNonVide(naturesDommage, "Au moins une nature de dommage doit être sélectionnée.");
    verifierChampObligatoire(description, "La description du dommage est obligatoire.");
    if (constatRequis()) {
      verifierChampObligatoire(constatPresent, "L'indication de constat est obligatoire.");
    }

    if (Boolean.TRUE.equals(constatPresent)) {
      verifierChampObligatoire(dateConstat,"La date du constat est obligatoire si un constat est présent.");
    }
  }

  private boolean constatRequis() {
    return typeDommage == TypeDommage.DOMMAGE_VEHICULE || typeDommage == TypeDommage.DOMMAGE_PROPRIETE;
  }

  @Override
  protected void verifierLongueurs() {
    super.verifierLongueurs();

    verifierLongueurMax(devise, TEXT_FIELD_MAX_LENGTH, "devise");
    verifierLongueurMax(description, TEXTAREA_MAX_LENGTH, "description");
    verifierLongueurMax(dateConstat, TEXT_FIELD_MAX_LENGTH, "dateConstat");
  }
}
