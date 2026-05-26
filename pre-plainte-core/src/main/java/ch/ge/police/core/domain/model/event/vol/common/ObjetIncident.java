package ch.ge.police.core.domain.model.event.vol.common;

import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.regex.Pattern;

/**
 * Représente un objet volé ou un véhicule volé.
 * Certains champs sont spécifiques à un type d'objet (ex: IMEI pour téléphone).
 * Les véhicules ont des champs supplémentaires (velofinderId, purchaseDate, vin).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ObjetIncident {

  private static final String CODE_TELEPHONE_MOBILE = "713103";
  private static final String CODE_PREFIX_CYCLOMOTEUR = "07";
  private static final String CODE_PREFIX_VELO = "20";
  private static final String CATEGORIE_PLAQUE = "plaque";
  private static final String CODE_AUTRE = "AUTRE";
  private static final Pattern NUMERO_IMEI_PATTERN = Pattern.compile("\\d{15}");

  private String categorieObjet;
  private String sousCategorie;
  private RipolCode type;
  private RipolCode fabricant;
  private String fabricantAutre;
  private RipolCode modele;
  private String modeleAutre;
  private RipolCode couleur;
  private RipolCode couleurSecondaire;
  private String numeroSerie;
  private boolean numeroSerieInconnu;
  private String numeroCadre;
  private boolean numeroCadreInconnu;
  private String numeroIMEI;
  private boolean numeroIMEIInconnu;
  private String justificationAbsenceIMEI;
  private String gravure;
  private String description;
  private String realValue;

  @JsonProperty("isVehicle")
  private boolean isVehicle;
  private String purchaseDate;
  private String vin;
  private boolean vinInconnu;
  private String velofinderId;

  private String plaqueNumero;
  private boolean plaqueInconnu;
  private RipolCode plaquePays;
  private RipolCode plaqueCanton;

  public String getTypeCode() {
    return type != null ? type.code() : null;
  }

  public String getTypeLabel() {
    return type != null ? type.label() : null;
  }

  public String getFabricantCode() {
    return fabricant != null ? fabricant.code() : null;
  }

  public String getFabricantLabel() {
    return fabricant != null ? fabricant.label() : null;
  }

  public String getModeleCode() {
    return modele != null ? modele.code() : null;
  }

  public String getModeleLabel() {
    return modele != null ? modele.label() : null;
  }

  public String getCouleurCode() {
    return couleur != null ? couleur.code() : null;
  }

  public String getCouleurLabel() {
    return couleur != null ? couleur.label() : null;
  }

  public String getCouleurSecondaireCode() {
    return couleurSecondaire != null ? couleurSecondaire.code() : null;
  }

  public String getCouleurSecondaireLabel() {
    return couleurSecondaire != null ? couleurSecondaire.label() : null;
  }

  public String getPlaquePaysCode() {
    return plaquePays != null ? plaquePays.code() : null;
  }

  public String getPlaquePaysLabel() {
    return plaquePays != null ? plaquePays.label() : null;
  }

  public String getPlaqueCantonCode() {
    return plaqueCanton != null ? plaqueCanton.code() : null;
  }

  public String getPlaqueCantonLabel() {
    return plaqueCanton != null ? plaqueCanton.label() : null;
  }

  public boolean isTelephoneMobile() {
    return type != null && CODE_TELEPHONE_MOBILE.equals(type.code());
  }

  /**
   * Détermine si l'objet est un véhicule.
   * Utilise le flag isVehicle du frontend OU la détection par code RIPOL.
   * Codes véhicules : "07" (cyclomoteurs), "20" (vélos).
   */
  public boolean isVehicleType() {
    if (CATEGORIE_PLAQUE.equals(categorieObjet)) {
      return false;
    }
    if (isVehicle) {
      return true;
    }
    if (type == null || type.code() == null) {
      return false;
    }
    String code = type.code();
    return code.startsWith(CODE_PREFIX_CYCLOMOTEUR) || code.startsWith(CODE_PREFIX_VELO);
  }

  public void champsObligatoire() {
    if (CATEGORIE_PLAQUE.equals(categorieObjet)) {
      if (plaquePays == null || !plaquePays.hasCode()) {
        throw new ValidationMetierException("Le pays de la plaque est obligatoire.");
      }
      if (plaqueNumero == null || plaqueNumero.isBlank()) {
        throw new ValidationMetierException("Le numéro de plaque est obligatoire.");
      }
      return;
    }

    if (type == null || !type.hasCode()) {
      throw new ValidationMetierException("Le type d'objet volé est obligatoire.");
    }

    if (isVehicleType()) {
      validateRipolSelection(fabricant, "La marque du vehicule est obligatoire.");
      validateAutreValue(fabricant, fabricantAutre, "La marque du vehicule doit etre precisee.");
      validateRipolSelection(modele, "Le modele du vehicule est obligatoire.");
      validateAutreValue(modele, modeleAutre, "Le modele du vehicule doit etre precise.");
    }

    if (isTelephoneMobile() && !numeroIMEIInconnu && (numeroIMEI == null || numeroIMEI.isBlank())) {
      throw new ValidationMetierException("Le numéro IMEI est obligatoire pour un téléphone volé.");
    }

    if (numeroIMEI != null && !numeroIMEI.isBlank() && !NUMERO_IMEI_PATTERN.matcher(numeroIMEI).matches()) {
      throw new ValidationMetierException("Le numéro IMEI doit contenir exactement 15 chiffres.");
    }
  }

  private void validateRipolSelection(RipolCode value, String message) {
    if (value == null || !value.hasCode()) {
      throw new ValidationMetierException(message);
    }
  }

  private void validateAutreValue(RipolCode value, String autreValue, String message) {
    if (value != null && CODE_AUTRE.equals(value.code()) && (autreValue == null || autreValue.isBlank())) {
      throw new ValidationMetierException(message);
    }
  }
}
