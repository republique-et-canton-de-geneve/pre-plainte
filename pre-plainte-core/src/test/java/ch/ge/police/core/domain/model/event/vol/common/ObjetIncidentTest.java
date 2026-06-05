package ch.ge.police.core.domain.model.event.vol.common;

import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjetIncidentTest {

  private static final RipolCode SUISSE = new RipolCode("8100", "Suisse");
  private static final RipolCode FRANCE = new RipolCode("8212", "France");

  @Test
  void shouldTreatPlaqueAsNonVehicleEvenWhenVehicleFlagIsTrue() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .isVehicle(true)
      .plaquePays(SUISSE)
      .plaqueNumero("GE123456")
      .build();

    assertFalse(objet.isVehicleType());
  }

  @Test
  void shouldNotTreatIdentityDocumentCodeAsVehicle() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("documents")
      .sousCategorie("papiers_identite")
      .type(new RipolCode("200300", "Passeport"))
      .build();

    assertFalse(objet.isVehicleType());
  }

  @Test
  void shouldAcceptSwissPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(SUISSE)
      .plaqueNumero("GE 123456")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectInvalidSwissPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(SUISSE)
      .plaqueNumero("123GE")
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptFrenchSivPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(FRANCE)
      .plaqueNumero("AA-123-AA")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldAcceptFrenchFniPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(FRANCE)
      .plaqueNumero("123 ABC 75")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectInvalidFrenchPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(FRANCE)
      .plaqueNumero("INVALID")
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptInternationalPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(new RipolCode("9999", "Autre"))
      .plaqueNumero("ABC123XYZ")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectInvalidInternationalPlate() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(new RipolCode("9999", "Autre"))
      .plaqueNumero("ABC-123")
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldRejectPlaqueWithoutPlateNumber() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaquePays(SUISSE)
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldRejectPlaqueWithoutPlateCountry() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .plaqueNumero("GE123456")
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldRejectObjectWithoutType() {
    ObjetIncident objet = ObjetIncident.builder()
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptObjectWithoutDescriptionWhenImeiUnknown() {
    ObjetIncident objet = ObjetIncident.builder()
      .type(new RipolCode("713103", "Telephone mobile"))
      .numeroIMEIInconnu(true)
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectMobilePhoneWithoutImei() {
    ObjetIncident objet = ObjetIncident.builder()
      .type(new RipolCode("713103", "Telephone mobile"))
      .numeroIMEIInconnu(false)
      .numeroIMEI(null)
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptValidImei() {
    ObjetIncident objet = ObjetIncident.builder()
      .type(new RipolCode("713103", "Telephone mobile"))
      .numeroIMEI("123456789012345")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectInvalidImeiFormat() {
    ObjetIncident objet = ObjetIncident.builder()
      .type(new RipolCode("713103", "Telephone mobile"))
      .numeroIMEI("IMEI123")
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldRejectVehicleWithoutBrandOrModel() {
    ObjetIncident objet = ObjetIncident.builder()
      .isVehicle(true)
      .type(new RipolCode("200", "Velo"))
      .categorieObjet("vehicule")
      .plaqueNumero("GE123456")
      .plaquePays(SUISSE)
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptVehicleWithBrandAndModel() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("vehicule")
      .isVehicle(true)
      .type(new RipolCode("200", "Velo"))
      .fabricant(new RipolCode("TREK", "Trek"))
      .modele(new RipolCode("DOMANE", "Domane"))
      .plaquePays(SUISSE)
      .plaqueCanton(new RipolCode("GE", "Genève"))
      .plaqueNumero("GE 123456")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectVehicleWithOtherBrandOrModelWithoutPrecision() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("vehicule")
      .isVehicle(true)
      .type(new RipolCode("200", "Velo"))
      .fabricant(new RipolCode("AUTRE", "Autre"))
      .modele(new RipolCode("AUTRE", "Autre"))
      .plaqueNumero("GE123456")
      .plaquePays(SUISSE)
      .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptVehicleWithOtherBrandAndModelPrecision() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("vehicule")
      .isVehicle(true)
      .type(new RipolCode("200", "Velo"))
      .fabricant(new RipolCode("AUTRE", "Autre"))
      .fabricantAutre("Marque custom")
      .modele(new RipolCode("AUTRE", "Autre"))
      .modeleAutre("Modele custom")
      .plaquePays(SUISSE)
      .plaqueCanton(new RipolCode("GE", "Genève"))
      .plaqueNumero("GE 123456")
      .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldReturnAssureurLabel() {
    ObjetIncident objet = ObjetIncident.builder()
      .assureur(new RipolCode("AXA", "AXA Assurance"))
      .build();

    assertEquals("AXA Assurance", objet.resolveAssureurNom());
  }

  @Test
  void shouldPreferAssureurAutre() {
    ObjetIncident objet = ObjetIncident.builder()
      .assureurAutre("  Mon assureur  ")
      .assuranceAucune(false)
      .build();

    assertEquals("Mon assureur", objet.resolveAssureurNom());
  }

  @Test
  void shouldReturnNullWhenNoInsurance() {
    ObjetIncident objet = ObjetIncident.builder()
      .assuranceAucune(true)
      .assureur(new RipolCode("AXA", "AXA"))
      .build();

    assertNull(objet.resolveAssureurNom());
  }

  @Test
  void shouldBeVehicleWhenCategoryIsVehicle() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("vehicule")
      .build();

    assertTrue(objet.isVehicleType());
  }

  @Test
  void shouldNotBeVehicleWhenCategoryIsPlaque() {
    ObjetIncident objet = ObjetIncident.builder()
      .categorieObjet("plaque")
      .build();

    assertFalse(objet.isVehicleType());
  }
}
