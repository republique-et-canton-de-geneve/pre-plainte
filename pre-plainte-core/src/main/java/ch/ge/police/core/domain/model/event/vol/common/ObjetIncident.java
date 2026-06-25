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

import java.util.List;
import java.util.regex.Pattern;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXTAREA_MAX_LENGTH;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

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
  private static final String CATEGORIE_VEHICULE = "vehicule";
  private static final String CATEGORIE_PLAQUE = "plaque";
  private static final String CATEGORIE_DOCUMENTS = "documents";

  private static final List<String> VEHICULE_CATEGORIES_AVEC_PLAQUE = List.of("motos", "voitures", "camions");

  private static final String CODE_AUTRE = "AUTRE";
  private static final String CODE_PAYS_SUISSE = "8100";
  private static final String CODE_PAYS_FRANCE = "8212";

  private static final Pattern NUMERO_IMEI_PATTERN = Pattern.compile("\\d{15}");
  private static final Pattern PLAQUE_SUISSE_PATTERN = Pattern.compile("^[A-Z]{2}\\s\\d{1,6}$");
  private static final Pattern PLAQUE_FRANCE_SIV_PATTERN = Pattern.compile("^[A-Z]{2}-\\d{3}-[A-Z]{2}$");
  private static final Pattern PLAQUE_FRANCE_FNI_PATTERN = Pattern.compile("^\\d{1,4}\\s[A-Z]{1,3}\\s(\\d{2,3}|2A|2B)$");
  private static final Pattern PLAQUE_INTERNATIONALE_PATTERN = Pattern.compile("^[A-Z\\d]{1,12}$");

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

  private boolean assuranceAucune;
  private RipolCode assureur;
  private String assureurAutre;
  private String numeroAssurance;
  private String numeroVignette;
  private String numeroMaster;

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

  public String getAssureurCode() {
    return assureur != null ? assureur.code() : null;
  }

  public String getAssureurLabel() {
    return assureur != null ? assureur.label() : null;
  }

  public String resolveAssureurNom() {
    if (assuranceAucune) {
      return null;
    }
    if (assureurAutre != null && !assureurAutre.isBlank()) {
      return assureurAutre.trim();
    }
    if (assureur != null && assureur.label() != null && !assureur.label().isBlank()) {
      return assureur.label().trim();
    }
    return null;
  }

  public boolean isTelephoneMobile() {
    return type != null && CODE_TELEPHONE_MOBILE.equals(type.code());
  }

  public boolean isVehicleType() {
    if (CATEGORIE_PLAQUE.equals(categorieObjet) || CATEGORIE_DOCUMENTS.equals(categorieObjet)) {
      return false;
    }
    return isVehicle || CATEGORIE_VEHICULE.equals(categorieObjet);
  }

  public void champsObligatoire() {
    verifierLongueurs();

    if (CATEGORIE_PLAQUE.equals(categorieObjet)) {
      validatePlaquePays();
      validatePlaqueNumero();
      return;
    }

    validateTypeEtCouleur();

    if (isVehicleType()) {
      validateFabricantSelection();
      validateFabricantAutreValue();
      if (!CODE_AUTRE.equals(fabricant.code())) {
        validateModele();
      }
      if (!plaqueInconnu && VEHICULE_CATEGORIES_AVEC_PLAQUE.contains(sousCategorie)) {
        validatePlaquePays();
        validatePlaqueCanton();
        validatePlaqueNumero();
      }
    }

    if (isTelephoneMobile() && !numeroIMEIInconnu && (numeroIMEI == null || numeroIMEI.isBlank())) {
      throw new ValidationMetierException("Le numéro IMEI est obligatoire pour un téléphone volé.");
    }

    if (numeroIMEI != null && !numeroIMEI.isBlank() && !NUMERO_IMEI_PATTERN.matcher(numeroIMEI).matches()) {
      throw new ValidationMetierException("Le numéro IMEI doit contenir exactement 15 chiffres.");
    }
  }

  private void validateTypeEtCouleur() {
    if (type == null || !type.hasCode()) {
      throw new ValidationMetierException("Le type d'objet volé est obligatoire.");
    }

    if (couleur == null || !couleur.hasCode()) {
      throw new ValidationMetierException("La couleur de l'objet est obligatoire.");
    }
  }

  private void validatePlaquePays() {
    if (plaquePays == null || !plaquePays.hasCode()) {
      throw new ValidationMetierException("Le pays de la plaque est obligatoire.");
    }
  }

  private void validatePlaqueCanton() {
    String paysCode = getPlaquePaysCode();

    if (CODE_PAYS_SUISSE.equals(paysCode) && (plaqueCanton == null || !plaqueCanton.hasCode())) {
      throw new ValidationMetierException("Le canton de la plaque est obligatoire pour le pays Suisse.");
    }
  }

  private void validatePlaqueNumero() {
    if (plaqueNumero == null || plaqueNumero.isBlank()) {
      throw new ValidationMetierException("Le numéro de plaque est obligatoire.");
    }

    String paysCode = getPlaquePaysCode();

    if (CODE_PAYS_SUISSE.equals(paysCode)) {
      if (!PLAQUE_SUISSE_PATTERN.matcher(plaqueNumero).matches()) {
        throw new ValidationMetierException("Le numéro de plaque suisse est invalide.");
      }
      return;
    }

    if (CODE_PAYS_FRANCE.equals(paysCode)) {
      boolean valid =
        PLAQUE_FRANCE_SIV_PATTERN.matcher(plaqueNumero).matches()
          || PLAQUE_FRANCE_FNI_PATTERN.matcher(plaqueNumero).matches();

      if (!valid) {
        throw new ValidationMetierException("Le numéro de plaque français est invalide.");
      }
      return;
    }

    if (!PLAQUE_INTERNATIONALE_PATTERN.matcher(plaqueNumero).matches()) {
      throw new ValidationMetierException("Le numéro de plaque est invalide.");
    }
  }

  private void validateFabricantSelection() {
    if (fabricant == null || !fabricant.hasCode()) {
      throw new ValidationMetierException("La marque du vehicule est obligatoire.");
    }
  }

  private void validateFabricantAutreValue() {
    if (fabricant != null && CODE_AUTRE.equals(fabricant.code()) && (fabricantAutre == null || fabricantAutre.isBlank())) {
      throw new ValidationMetierException("La marque du vehicule doit etre precisee.");
    }
  }

  private void validateModele() {
    if ((modele == null || !modele.hasCode()) && (modeleAutre == null || modeleAutre.isBlank())) {
      throw new ValidationMetierException("Le modele du vehicule doit etre precise.");
    }
  }

  private void verifierLongueurs() {
    verifierLongueurMax(categorieObjet, TEXT_FIELD_MAX_LENGTH, "categorieObjet");
    verifierLongueurMax(sousCategorie, TEXT_FIELD_MAX_LENGTH, "sousCategorie");

    verifierLongueurMax(fabricantAutre, TEXT_FIELD_MAX_LENGTH, "fabricantAutre");
    verifierLongueurMax(modeleAutre, TEXT_FIELD_MAX_LENGTH, "modeleAutre");

    verifierLongueurMax(numeroSerie, TEXT_FIELD_MAX_LENGTH, "numeroSerie");
    verifierLongueurMax(numeroCadre, TEXT_FIELD_MAX_LENGTH, "numeroCadre");

    verifierLongueurMax(justificationAbsenceIMEI, TEXTAREA_MAX_LENGTH, "justificationAbsenceIMEI");

    verifierLongueurMax(gravure, TEXT_FIELD_MAX_LENGTH, "gravure");
    verifierLongueurMax(realValue, TEXT_FIELD_MAX_LENGTH, "realValue");

    verifierLongueurMax(purchaseDate, TEXT_FIELD_MAX_LENGTH, "purchaseDate");

    verifierLongueurMax(vin, TEXT_FIELD_MAX_LENGTH, "vin");
    verifierLongueurMax(velofinderId, TEXT_FIELD_MAX_LENGTH, "velofinderId");

    verifierLongueurMax(assureurAutre, TEXT_FIELD_MAX_LENGTH, "assureurAutre");
    verifierLongueurMax(numeroAssurance, TEXT_FIELD_MAX_LENGTH, "numeroAssurance");
    verifierLongueurMax(numeroVignette, TEXT_FIELD_MAX_LENGTH, "numeroVignette");
    verifierLongueurMax(numeroMaster, TEXT_FIELD_MAX_LENGTH, "numeroMaster");
  }
}
