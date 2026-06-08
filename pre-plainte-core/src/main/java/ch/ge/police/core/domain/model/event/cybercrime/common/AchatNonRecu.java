package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.Adresse;
import lombok.Data;

import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierChampObligatoire;
import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXTAREA_MAX_LENGTH;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

/**
 * Détails pour un cybercrime de type "achat non reçu".
 */
@Data
public class AchatNonRecu {
  private String montantDelitAchatLigne;
  private String articleNonLivreDescription;
  private String prenomVendeur;
  private String nomVendeur;
  private Boolean telephoneVendeurInconnu;
  private String telephoneVendeur;
  private Boolean emailVendeurInconnu;
  private String emailVendeur;
  private Boolean adresseVendeurInconnue;
  private Adresse adresseVendeur;
  private Boolean achatViaPlaceMarche;
  private PlateformeUtilisee plateformeUtilisee;
  private String plateformeAutre;
  private String plateformeId;
  private String nomEntrepriseVendeur;
  private String siteWebEntrepriseVendeur;
  private Boolean annonceDocumentIndisponible;
  private String raisonAbsenceAnnonce;
  private MoyenPaiement moyenPaiement;
  private String moyenPaiementAutre;
  private String ibanBeneficiaire;
  private String comptePaypalBeneficiaire;
  private String numeroTwintBeneficiaire;
  private String adresseWalletCrypto;
  private String hashTransactionCrypto;
  private String societeBeneficiaire;
  private String nomBeneficiaire;
  private String prenomBeneficiaire;
  private String dateOperation;
  private Boolean preuvePaiementIndisponible;
  private String raisonAbsencePreuvePaiement;
  private Boolean copieIdentiteTransmiseAuteur;
  private Boolean copieIdentiteAuteurTransmise;

  public void champsObligatoireCybercrime() {
    verifierChampsObligatoires();
    verifierLongueurs();

    if (adresseVendeur != null) {
      adresseVendeur.validate();
    }
  }

  private void verifierChampsObligatoires() {
    verifierChampObligatoire(prenomVendeur, "Le prénom du vendeur est obligatoire.");
    verifierChampObligatoire(nomVendeur, "Le nom du vendeur est obligatoire.");
    verifierChampObligatoire(moyenPaiement, "Le moyen de paiement est obligatoire.");
    verifierChampObligatoire(dateOperation, "La date de l’opération est obligatoire.");
  }

  private void verifierLongueurs() {
    verifierLongueurMax(prenomVendeur, TEXT_FIELD_MAX_LENGTH, "prenomVendeur");
    verifierLongueurMax(nomVendeur, TEXT_FIELD_MAX_LENGTH, "nomVendeur");

    verifierLongueurMax(articleNonLivreDescription, TEXTAREA_MAX_LENGTH, "articleNonLivreDescription");

    verifierLongueurMax(emailVendeur, TEXT_FIELD_MAX_LENGTH, "emailVendeur");
    verifierLongueurMax(telephoneVendeur, TEXT_FIELD_MAX_LENGTH, "telephoneVendeur");

    verifierLongueurMax(plateformeAutre, TEXT_FIELD_MAX_LENGTH, "plateformeAutre");
    verifierLongueurMax(plateformeId, TEXT_FIELD_MAX_LENGTH, "plateformeId");

    verifierLongueurMax(nomEntrepriseVendeur, TEXT_FIELD_MAX_LENGTH, "nomEntrepriseVendeur");
    verifierLongueurMax(siteWebEntrepriseVendeur, TEXT_FIELD_MAX_LENGTH, "siteWebEntrepriseVendeur");

    verifierLongueurMax(raisonAbsenceAnnonce, TEXTAREA_MAX_LENGTH, "raisonAbsenceAnnonce");
    verifierLongueurMax(moyenPaiementAutre, TEXT_FIELD_MAX_LENGTH, "moyenPaiementAutre");

    verifierLongueurMax(ibanBeneficiaire, TEXT_FIELD_MAX_LENGTH, "ibanBeneficiaire");
    verifierLongueurMax(comptePaypalBeneficiaire, TEXT_FIELD_MAX_LENGTH, "comptePaypalBeneficiaire");
    verifierLongueurMax(numeroTwintBeneficiaire, TEXT_FIELD_MAX_LENGTH, "numeroTwintBeneficiaire");

    verifierLongueurMax(adresseWalletCrypto, TEXT_FIELD_MAX_LENGTH, "adresseWalletCrypto");
    verifierLongueurMax(hashTransactionCrypto, TEXT_FIELD_MAX_LENGTH, "hashTransactionCrypto");

    verifierLongueurMax(societeBeneficiaire, TEXT_FIELD_MAX_LENGTH, "societeBeneficiaire");
    verifierLongueurMax(nomBeneficiaire, TEXT_FIELD_MAX_LENGTH, "nomBeneficiaire");
    verifierLongueurMax(prenomBeneficiaire, TEXT_FIELD_MAX_LENGTH, "prenomBeneficiaire");

    verifierLongueurMax(dateOperation, TEXT_FIELD_MAX_LENGTH, "dateOperation");
    verifierLongueurMax(raisonAbsencePreuvePaiement, TEXTAREA_MAX_LENGTH, "raisonAbsencePreuvePaiement");
  }
}
