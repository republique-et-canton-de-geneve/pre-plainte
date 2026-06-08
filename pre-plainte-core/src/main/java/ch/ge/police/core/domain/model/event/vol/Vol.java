package ch.ge.police.core.domain.model.event.vol;

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

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierCollectionNonVide;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Vol extends IncidentBase {

  private Boolean volDansVehicule;
  private List<ObjetIncident> objetsVoles;
  private Boolean avezVousDegradation;

  @Override
  public TypeIncident getTypeIncident() {
    return TypeIncident.VOL;
  }

  @Override
  public void champsObligatoireIncident() {
    super.champsObligatoireIncident();

    verifierChampsObligatoires();
    objetsVoles.forEach(ObjetIncident::champsObligatoire);
  }

  @Override
  protected void verifierChampsObligatoires() {
    super.verifierChampsObligatoires();

    verifierChampObligatoire(getDateDebutEvent(), "La date de début d'événement est obligatoire.");
    verifierChampObligatoire(getDateFinEvent(), "La date de fin d'événement est obligatoire.");
    verifierChampObligatoire(volDansVehicule, "Veuillez renseigner si le vol s'est deroulé dans un véhicule ou non");
    verifierChampObligatoire(avezVousDegradation, "Veuillez renseigner si il y a eu des dégradations ou non");
    verifierCollectionNonVide(objetsVoles, "Au moins un objet doit être renseigné");
  }
}
