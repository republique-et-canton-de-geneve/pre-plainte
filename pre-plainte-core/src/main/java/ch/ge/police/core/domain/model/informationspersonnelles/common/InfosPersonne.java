package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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

  /**
   * Méthode interne de validation générique.
   * Vérifie qu'une valeur (String ou Object) n'est pas nulle
   * et, dans le cas d'une chaîne, non vide ou blanche.
   */
  protected void verifierChampObligatoire(Object value, String messageErreur) {
    if (value == null) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof String valueStr && valueStr.isBlank()) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof RipolCode rc && !rc.hasCode()) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  public void validateBasicInfo() {
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
      verifierChampObligatoire(numeroDocumentIdentite, "Le numéro de document d'identité est obligatoire.");
    }
  }
}
