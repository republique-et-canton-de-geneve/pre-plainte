package ch.ge.police.core.domain.model.event.common;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Incident {

  private final TypeIncident typeIncident;
  private final IncidentBase details;

  @JsonCreator
  public Incident(
    @JsonProperty("typeIncident") TypeIncident typeIncident,
    @JsonProperty("details") IncidentBase details
  ) {
    this.typeIncident = typeIncident;
    this.details = details;
  }

  /** Fabrique un incident à partir d’un type concret (ex: DommageMateriel, Vol, etc.) */
  public static Incident of(IncidentBase details) {
    if (details == null) {
      throw new ValidationMetierException("Les détails de l’incident ne peuvent pas être nuls.");
    }

    return new Incident(details.getTypeIncident(), details);
  }


  public void validate() {
    if (details == null) {
      throw new ValidationMetierException("Aucun type d’incident n’a été fourni.");
    }

    if (typeIncident == null) {
      throw new ValidationMetierException("Le type d’incident est obligatoire.");
    }

    if (typeIncident != details.getTypeIncident()) {
      throw new ValidationMetierException(String.format(
        "Incohérence entre le type d’incident (%s) et l’objet fourni (%s).",
        typeIncident, details.getTypeIncident()
      ));
    }

    details.champsObligatoireIncident();
  }
}
