package ch.ge.police.core.domain.model.event.vol;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires corrigés pour la validation de la classe Vol.
 */
class VolTest {

  private Vol vol;

  @BeforeEach
  void setUp() {
    vol = new Vol();

    vol.setDateDebutEvent("2025-10-21");
    vol.setDateFinEvent("2025-10-21");
    vol.setAdresseIncident(new Adresse("Rue du Marché 10", "1204 Genève", "1204", "Genève", "6621", "Suisse", "8100"));

    vol.setVolDansVehicule(false);
    vol.setObjetsVoles(List.of(ObjetIncident.builder()
        .type(new RipolCode("713103", "Téléphone mobile"))
        .fabricant(new RipolCode("35839", "Apple"))
        .modele(new RipolCode("12345", "iPhone 14"))
        .numeroSerie("SN001")
        .numeroIMEI("123456789012345")
        .description("Volé sur la table")
        .build()));
    vol.setAvezVousDegradation(true);
  }

  @Test
  void shouldPassValidationWithAllRequiredFields() {
    assertDoesNotThrow(vol::champsObligatoireIncident);
  }

  @Test
  void shouldThrowWhenVolDansVehiculeMissing() {
    vol.setVolDansVehicule(null);
    Exception ex = assertThrows(ValidationMetierException.class, vol::champsObligatoireIncident);
    assertEquals("Veuillez renseigner si le vol s'est deroulé dans un véhicule ou non", ex.getMessage());
  }

  @Test
  void shouldThrowWhenObjetsVolesMissing() {
    vol.setObjetsVoles(null);
    Exception ex = assertThrows(ValidationMetierException.class, vol::champsObligatoireIncident);
    assertEquals("Au moins un objet doit être renseigné", ex.getMessage());
  }

  @Test
  void shouldThrowWhenDegradationMissing() {
    vol.setAvezVousDegradation(null);
    Exception ex = assertThrows(ValidationMetierException.class, vol::champsObligatoireIncident);
    assertEquals("Veuillez renseigner si il y a eu des dégradations ou non", ex.getMessage());
  }

  @Test
  void shouldPassWhenAllFieldsAreValid() {
    assertDoesNotThrow(vol::champsObligatoireIncident);
  }
}
