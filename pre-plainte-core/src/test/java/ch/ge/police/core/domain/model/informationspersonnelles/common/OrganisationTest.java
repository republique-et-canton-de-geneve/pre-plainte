package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;

class OrganisationTest {

  private Organisation createValidOrganisation() {
    Organisation org = new Organisation();
    org.setNom("Keylity SA");
    org.setAdresse(new Adresse("Rue du Marché 10", "Genève", "1204", "CH", "1234", "Suisse", "8212"));
    org.setTelephone("41220000000");
    org.setEmail("contact@keylity.ch");
    return org;
  }

  @Test
  void shouldValidateSuccessfully() {
    Organisation org = createValidOrganisation();
    assertDoesNotThrow(org::validateOrganisationInfo);
  }

  @Test
  void shouldThrowWhenAdresseMissing() {
    Organisation org = createValidOrganisation();
    org.setAdresse(null);
    Exception ex = assertThrows(ValidationMetierException.class, org::validateOrganisationInfo);
    assertTrue(ex.getMessage().toLowerCase().contains("adresse"));
  }

  @Test
  void shouldThrowWhenEmailMissing() {
    Organisation org = createValidOrganisation();
    org.setEmail("");
    Exception ex = assertThrows(ValidationMetierException.class, org::validateOrganisationInfo);
    assertTrue(ex.getMessage().toLowerCase().contains("e-mail") || ex.getMessage().toLowerCase().contains("email"));
  }
}
