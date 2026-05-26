package ch.ge.police.core.domain.model.event.vol.common;

import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjetIncidentTest {

  @Test
  void shouldTreatPlaqueAsNonVehicleEvenWhenVehicleFlagIsTrue() {
    ObjetIncident objet = ObjetIncident.builder()
        .categorieObjet("plaque")
        .isVehicle(true)
        .plaquePays(new RipolCode("8100", "Suisse"))
        .plaqueNumero("GE123456")
        .build();

    assertFalse(objet.isVehicleType());
  }

  @Test
  void shouldAcceptPlaqueWithPlateNumberWithoutObjectTypeOrDescription() {
    ObjetIncident objet = ObjetIncident.builder()
        .categorieObjet("plaque")
        .plaquePays(new RipolCode("8100", "Suisse"))
        .plaqueNumero("GE123456")
        .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectPlaqueWithoutPlateNumber() {
    ObjetIncident objet = ObjetIncident.builder()
        .categorieObjet("plaque")
        .plaquePays(new RipolCode("8100", "Suisse"))
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
  void shouldAcceptObjectWithoutDescription() {
    ObjetIncident objet = ObjetIncident.builder()
        .type(new RipolCode("713103", "Telephone mobile"))
        .numeroIMEIInconnu(true)
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
        .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }

  @Test
  void shouldAcceptVehicleWithBrandAndModel() {
    ObjetIncident objet = ObjetIncident.builder()
        .isVehicle(true)
        .type(new RipolCode("200", "Velo"))
        .fabricant(new RipolCode("TREK", "Trek"))
        .modele(new RipolCode("DOMANE", "Domane"))
        .build();

    assertDoesNotThrow(objet::champsObligatoire);
  }

  @Test
  void shouldRejectVehicleWithOtherBrandOrModelWithoutPrecision() {
    ObjetIncident objet = ObjetIncident.builder()
        .isVehicle(true)
        .type(new RipolCode("200", "Velo"))
        .fabricant(new RipolCode("AUTRE", "Autre"))
        .modele(new RipolCode("AUTRE", "Autre"))
        .build();

    assertThrows(ValidationMetierException.class, objet::champsObligatoire);
  }
}
