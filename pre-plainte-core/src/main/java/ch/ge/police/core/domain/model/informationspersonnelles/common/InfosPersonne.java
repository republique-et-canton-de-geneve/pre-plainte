package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Informations de base d'une personne physique.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfosPersonne {

  private String nom;
  private String nomNaissance;
  private String prenom;
  private RipolCode genre;
  private RipolCode nationalite;
  private RipolCode lieuOrigine;
  private TitreSejour titreSejour;
  private String dateNaissance;
  private Adresse adresse;
  private String telephone;
  private String email;
  private TypeDocumentIdentite typeDocumentIdentite;
  private String numeroDocumentIdentite;

  public String getGenreCode() {
    return genre != null ? genre.code() : null;
  }

  public String getGenreLabel() {
    return genre != null ? genre.label() : null;
  }

  public String getNationaliteCode() {
    return nationalite != null ? nationalite.code() : null;
  }

  public String getNationaliteLabel() {
    return nationalite != null ? nationalite.label() : null;
  }

  public String getLieuOrigineCode() {
    return lieuOrigine != null ? lieuOrigine.code() : null;
  }

  public String getLieuOrigineLabel() {
    return lieuOrigine != null ? lieuOrigine.label() : null;
  }

  public void validateBasicInfo() {
    verifierChampsObligatoires();
    verifierLongueurs();

    if (adresse != null) {
      adresse.validate();
    }
  }

  private void verifierChampsObligatoires() {
    verifierChampObligatoire(nom, "Le nom est obligatoire.");
    verifierChampObligatoire(prenom, "Le prénom est obligatoire.");
    verifierChampObligatoire(genre, "Le genre est obligatoire.");
    verifierChampObligatoire(nationalite, "La nationalité est obligatoire.");
    verifierChampObligatoire(dateNaissance, "La date de naissance est obligatoire.");
    verifierChampObligatoire(adresse, "L'adresse est obligatoire.");
    verifierChampObligatoire(telephone, "Le numéro de téléphone est obligatoire.");
    verifierChampObligatoire(email, "L'adresse e-mail est obligatoire.");
    verifierChampObligatoire(typeDocumentIdentite, "Le type de document d'identité est obligatoire.");

    if (typeDocumentIdentite != TypeDocumentIdentite.DOCUMENTS_VOLES_PERDUS) {
      verifierChampObligatoire(
        numeroDocumentIdentite,
        "Le numéro de document d'identité est obligatoire."
      );
    }
  }

  private void verifierLongueurs() {
    verifierLongueurMax(nom, TEXT_FIELD_MAX_LENGTH, "nom");
    verifierLongueurMax(nomNaissance, TEXT_FIELD_MAX_LENGTH, "nomNaissance");
    verifierLongueurMax(prenom, TEXT_FIELD_MAX_LENGTH, "prenom");
    verifierLongueurMax(dateNaissance, TEXT_FIELD_MAX_LENGTH, "dateNaissance");
    verifierLongueurMax(telephone, TEXT_FIELD_MAX_LENGTH, "telephone");
    verifierLongueurMax(email, TEXT_FIELD_MAX_LENGTH, "email");
    verifierLongueurMax(numeroDocumentIdentite, TEXT_FIELD_MAX_LENGTH, "numeroDocumentIdentite");
  }
}
