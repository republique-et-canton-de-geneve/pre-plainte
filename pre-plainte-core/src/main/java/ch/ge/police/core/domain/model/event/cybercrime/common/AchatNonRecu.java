package ch.ge.police.core.domain.model.event.cybercrime.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import lombok.Data;

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

    if (prenomVendeur == null || prenomVendeur.isBlank()) {
      throw new ValidationMetierException("Le prénom du vendeur est obligatoire.");
    }

    if (nomVendeur == null || nomVendeur.isBlank()) {
      throw new ValidationMetierException("Le nom du vendeur est obligatoire.");
    }

    if (moyenPaiement == null || moyenPaiement.getCode() == null || moyenPaiement.getCode().isBlank()) {
      throw new ValidationMetierException("Le moyen de paiement est obligatoire.");
    }

    if (dateOperation == null || dateOperation.isBlank()) {
      throw new ValidationMetierException("La date de l’opération est obligatoire.");
    }
  }
}
