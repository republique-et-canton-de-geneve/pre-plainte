package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.Adresse;
import lombok.Data;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Détails pour un cybercrime de type "commande frauduleuse".
 */
@Data
public class CommandeFrauduleuse {
  private String prestataire;
  private String dateDecouverte;
  private Double montant;
  private Boolean assurance;
  private Boolean emailCommandeInconnu;
  private String emailCommande;
  private Boolean telephoneCommandeInconnu;
  private String telephoneCommande;
  private Boolean livraisonAdresseLesee;
  private Adresse adresseLivraison;
  private String prenomContrevenant;
  private String nomContrevenant;
  private Adresse adresseContrevenant;
  private String siteWebContrevenant;
  private Boolean moyenPaiementNumeriqueDebite;
  private Boolean copieIdentiteTransmiseAuteur;
  private Boolean copieIdentiteTransmiseAuteurDocumentIndisponible;
  private String raisonAbsenceCopieIdentiteTransmiseAuteur;
  private Boolean copieIdentiteAuteurTransmise;
  private Boolean copieIdentiteAuteurDocumentIndisponible;
  private String raisonAbsenceCopieIdentiteAuteur;

  public void champsObligatoireCybercrime() {
    verifierChampsObligatoires();
    verifierLongueurs();

    if (adresseLivraison != null) {
      adresseLivraison.validate();
    }
    if (adresseContrevenant != null) {
      adresseContrevenant.validate();
    }
  }

  private void verifierChampsObligatoires() {
    verifierChampObligatoire(dateDecouverte, "La date de découverte est obligatoire.");
    verifierChampObligatoire(montant, "Le montant du délit est obligatoire.");
    verifierChampObligatoire(assurance, "L’information sur l’assurance est obligatoire.");
  }

  private void verifierLongueurs() {
    verifierLongueurMax(prestataire, TEXT_FIELD_MAX_LENGTH, "prestataire");
    verifierLongueurMax(dateDecouverte, TEXT_FIELD_MAX_LENGTH, "dateDecouverte");
    verifierLongueurMax(emailCommande, TEXT_FIELD_MAX_LENGTH, "emailCommande");
    verifierLongueurMax(telephoneCommande, TEXT_FIELD_MAX_LENGTH, "telephoneCommande");
    verifierLongueurMax(prenomContrevenant, TEXT_FIELD_MAX_LENGTH, "prenomContrevenant");
    verifierLongueurMax(nomContrevenant, TEXT_FIELD_MAX_LENGTH, "nomContrevenant");
    verifierLongueurMax(siteWebContrevenant, TEXT_FIELD_MAX_LENGTH, "siteWebContrevenant");
    verifierLongueurMax(raisonAbsenceCopieIdentiteTransmiseAuteur, TEXT_FIELD_MAX_LENGTH, "raisonAbsenceCopieIdentiteTransmiseAuteur");
    verifierLongueurMax(raisonAbsenceCopieIdentiteAuteur, TEXT_FIELD_MAX_LENGTH, "raisonAbsenceCopieIdentiteAuteur");
  }
}
