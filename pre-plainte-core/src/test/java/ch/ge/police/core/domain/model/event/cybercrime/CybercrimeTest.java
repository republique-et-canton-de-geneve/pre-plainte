package ch.ge.police.core.domain.model.event.cybercrime;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.MoyenPaiement;
import ch.ge.police.core.domain.model.event.cybercrime.common.PlateformeUtilisee;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la validation de Cybercrime.
 */
class CybercrimeTest {

  private Cybercrime createValidCybercrimeBase() {
    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setDateDebutEvent("2025-10-21");
    cybercrime.setDateFinEvent("2025-10-22");
    cybercrime.setAdresseIncident(new Adresse("Rue du Rhône 1", "Genève", "1201", "CH", null, "Suisse", null));
    cybercrime.setDescriptionCybercrime("Achat non reçu d’un téléphone");
    return cybercrime;
  }

  @Test
  void shouldPassValidationWithValidAchatNonRecu() {
    Cybercrime cybercrime = createValidCybercrimeBase();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setDatePremierContact("2025-10-21");
    cybercrime.setDateDernierContact("2025-10-22");

    AchatNonRecu achat = new AchatNonRecu();
    achat.setPrenomVendeur("Jean");
    achat.setNomVendeur("Dupont");
    achat.setMoyenPaiement(MoyenPaiement.TWINT);
    achat.setPlateformeUtilisee(PlateformeUtilisee.TUTTI);
    achat.setDateOperation("2025-10-01");
    cybercrime.setAchatNonRecu(achat);

    assertDoesNotThrow(cybercrime::champsObligatoireIncident);
  }

  @Test
  void shouldThrowWhenMissingSubtype() {
    Cybercrime cybercrime = createValidCybercrimeBase();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setDatePremierContact("2025-10-21");
    cybercrime.setDateDernierContact("2025-10-22");

    Exception ex = assertThrows(ValidationMetierException.class, cybercrime::champsObligatoireIncident);
    assertTrue(ex.getMessage().contains("sous-type") || ex.getMessage().contains("achat non reçu"));
  }

  @Test
  void shouldThrowWhenMandatoryFieldMissing() {
    Cybercrime cybercrime = createValidCybercrimeBase();
    cybercrime.setTypeCybercrime(null);

    Exception ex = assertThrows(ValidationMetierException.class, cybercrime::champsObligatoireIncident);
    assertEquals("Le type de cybercrime est obligatoire.", ex.getMessage());
  }

  @Test
  void shouldValidateCommandeFrauduleuse() {
    Cybercrime cybercrime = createValidCybercrimeBase();
    cybercrime.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);

    CommandeFrauduleuse cmd = new CommandeFrauduleuse();
    cmd.setDateDecouverte("2025-09-10");
    cmd.setMontant(350.0);
    cmd.setAssurance(true);
    cybercrime.setCommandeFrauduleuse(cmd);

    assertDoesNotThrow(cybercrime::champsObligatoireIncident);
  }

  @Test
  void shouldValidateFausseAnnonce() {
    Cybercrime cybercrime = createValidCybercrimeBase();
    cybercrime.setTypeCybercrime(TypeCybercrime.FAUSSE_ANNONCE);
    cybercrime.setDatePremierContact("2025-10-21");
    cybercrime.setDateDernierContact("2025-10-22");

    FausseAnnonce annonce = new FausseAnnonce();
    annonce.setUrlComplete("https://facebook.ch");
    annonce.setTitreAnnonce("Appartement à louer");
    annonce.setNomBailleur("Martin");
    annonce.setEmailBailleur("martin@test.ch");
    annonce.setMontantDemande(1200.0);
    annonce.setModePaiementDemande("Virement bancaire");
    cybercrime.setFausseAnnonce(annonce);

    assertDoesNotThrow(cybercrime::champsObligatoireIncident);
  }
}
