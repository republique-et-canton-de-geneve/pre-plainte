package ch.ge.police.core.domain.model.event;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.fichier.Fichier;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Classe de base pour tout incident.
 * Contient les informations minimales obligatoires
 * présentes dans toutes les variantes d’incidents.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "typeIncident"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel.class, name = "dommageMateriel"),
  @JsonSubTypes.Type(value = ch.ge.police.core.domain.model.event.vol.Vol.class, name = "vol"),
  @JsonSubTypes.Type(value = ch.ge.police.core.domain.model.event.cybercrime.Cybercrime.class, name = "cybercrime")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class IncidentBase {


  private String dateDebutEvent;
  private String dateFinEvent;
  private Adresse adresseIncident;
  private Adresse adresseIncidentSecondaire;
  private RipolCode typeLieu;
  private String lieuOrigine;
  private Boolean adresseConnue;
  private Boolean adresseLesee;
  private Boolean isTrajet;
  private List<Fichier> fichiers;

  public String getTypeLieuLabel() {
    return typeLieu != null ? typeLieu.label() : null;
  }

  protected void validateurChampsObligatoires(Object valeur, String messageErreur) {
    if (valeur == null || (valeur instanceof String s && s.isBlank())) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  public void champsObligatoireIncident() {
    validateurChampsObligatoires(adresseIncident, "L’adresse de l’incident est obligatoire.");
  }

  /** Chaque sous-classe doit définir le type de l’incident */
  public abstract TypeIncident getTypeIncident();
}
