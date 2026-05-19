package ch.ge.police.core.domain.model.informationspersonnelles;

import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.common.InfosPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Représente les informations personnelles du déclarant,
 * pouvant inclure :
 * - un tiers concerné (si déclaration pour quelqu'un d'autre)
 * - une personne morale (entreprise / association)
 */

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InformationsPersonnelles extends InfosPersonne {

  private LienAvecPersonne lienAvecPersonne;
  private String typeRepresentation;
  private String postePersonneMorale;
  private Fichier justificatifPersonneMorale;
  private Boolean parlesFrancais;
  private String langueCorrespondance;

  private Tiers tiers;
  private Organisation organisation;

  public boolean hasTiers() {
    return lienAvecPersonne == LienAvecPersonne.TIERS;
  }

  public boolean hasOrganisation() {
    return lienAvecPersonne == LienAvecPersonne.ENTREPRISE;
  }

  /**
   * Valide la cohérence métier des informations personnelles.
   * Les validations sont strictement liées à la logique du domaine.
   */
  public void validate() {

    verifierChampObligatoire(lienAvecPersonne, "Le lien avec la personne concernée est obligatoire.");
    super.validateBasicInfo();

    if (Boolean.FALSE.equals(parlesFrancais)) {
      verifierChampObligatoire(
        langueCorrespondance,
        "La langue de correspondance est obligatoire si la personne ne parle pas français."
      );
    }
    if (hasTiers()) {
      verifierChampObligatoire(tiers, "Les informations du tiers sont obligatoires.");
      verifierChampObligatoire(typeRepresentation, "Le type de représentation est obligatoire.");
      tiers.validateBasicInfo();
    }

    if (hasOrganisation()) {
      verifierChampObligatoire(organisation, "Les informations de l’organisation sont obligatoires.");
      verifierChampObligatoire(postePersonneMorale, "Le poste ou fonction est obligatoire.");
      organisation.validateOrganisationInfo();
    }
  }
}
