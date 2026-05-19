package ch.ge.police.core.domain.model.event.vol;

import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Vol extends IncidentBase {

  private Boolean volDansVehicule;
  private String categorieObjet;
  private List<ObjetIncident> objetsVoles;
  private Boolean avezVousDegradation;

  @Override
  public TypeIncident getTypeIncident() {
    return TypeIncident.VOL;
  }

  @Override
  public void champsObligatoireIncident() {
    super.champsObligatoireIncident();

    validateurChampsObligatoires(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    validateurChampsObligatoires(getDateFinEvent(), "La date de fin d'événement est obligatoire.");

    champsObligatoireVol(volDansVehicule, "Veuillez renseigner si le vol s'est deroulé dans un véhicule ou non");
    if (objetsVoles == null || objetsVoles.isEmpty()) {
      throw new ValidationMetierException("Au moins un objet doit être renseigné");
    }
    champsObligatoireVol(avezVousDegradation, "Veuillez renseigner si il y a eu des dégradations ou non");
    objetsVoles.forEach(ObjetIncident::champsObligatoire);
  }


  public void champsObligatoireVol(Object value, String messageErreur) {
    if (value == null) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof String valueStr && valueStr.isBlank()) {
      throw new ValidationMetierException(messageErreur);
    }
  }
}
