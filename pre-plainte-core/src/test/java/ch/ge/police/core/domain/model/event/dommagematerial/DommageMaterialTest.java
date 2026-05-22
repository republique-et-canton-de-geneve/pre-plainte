package ch.ge.police.core.domain.model.event.dommagematerial;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la validation de DommageMateriel.
 */
class DommageMaterielTest {

  private DommageMateriel dommage;

  @BeforeEach
  void setUp() {
    dommage = new DommageMateriel();
    dommage.setDateDebutEvent("2025-10-21");
    dommage.setDateFinEvent("2025-10-21");
    dommage.setAdresseIncident(new Adresse("Rue du Rhône 1", "1201 Genève", "1201", "Genève", "6621", "Suisse", "8100"));

    dommage.setTypeDommage(TypeDommage.DOMMAGE_PROPRIETE);
    dommage.setMontantEstime(2500.0);
    dommage.setDevise("CHF");
    dommage.setNaturesDommage(List.of(NatureDommage.TAGS_GRAFFITI));
    dommage.setDescription("Dommages sur la façade principale");
    dommage.setConstatPresent(true);
    dommage.setDateConstat("2025-10-22");
  }

  @Test
  void shouldPassValidationWithAllRequiredFields() {
    assertDoesNotThrow(dommage::champsObligatoireIncident);
  }

  @Test
  void shouldThrowWhenTypeDommageMissing() {
    dommage.setTypeDommage(null);
    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("Le type de dommage doit être sélectionné.", ex.getMessage());
  }

  @Test
  void shouldPassWhenMontantEstimeMissing() {
    dommage.setMontantEstime(null);

    assertDoesNotThrow(dommage::champsObligatoireIncident);
  }

  @Test
  void shouldThrowWhenDeviseMissing() {
    dommage.setDevise(null);
    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("La devise est obligatoire.", ex.getMessage());
  }

  @Test
  void shouldThrowWhenNatureDommageMissing() {
    dommage.setNaturesDommage(null);
    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("Au moins une nature de dommage doit être sélectionnée.", ex.getMessage());
  }

  @Test
  void shouldThrowWhenDescriptionMissing() {
    dommage.setDescription("");
    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("La description du dommage est obligatoire.", ex.getMessage());
  }

  @Test
  void shouldThrowWhenConstatPresentMissing() {
    dommage.setConstatPresent(null);
    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("L'indication de constat est obligatoire.", ex.getMessage());
  }

  @Test
  void shouldThrowWhenDateConstatMissingIfConstatTrue() {
    dommage.setConstatPresent(true);
    dommage.setDateConstat(null);

    Exception ex = assertThrows(ValidationMetierException.class, dommage::champsObligatoireIncident);
    assertEquals("La date du constat est obligatoire si un constat est présent.", ex.getMessage());
  }

  @Test
  void shouldPassWhenConstatIsFalseAndDateConstatNotRequired() {
    dommage.setConstatPresent(false);
    dommage.setDateConstat(null);

    assertDoesNotThrow(dommage::champsObligatoireIncident);
  }
}
