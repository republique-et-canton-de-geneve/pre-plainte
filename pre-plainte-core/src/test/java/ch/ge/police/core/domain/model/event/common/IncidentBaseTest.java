package ch.ge.police.core.domain.model.event.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de validation sur la classe IncidentBase.
 */
class IncidentBaseTest {

  static class FakeIncident extends IncidentBase {
    @Override
    public TypeIncident getTypeIncident() {
      return TypeIncident.VOL;
    }
  }

  private FakeIncident createValidIncident() {
    FakeIncident incident = new FakeIncident();
    incident.setDateDebutEvent("2025-10-21");
    incident.setDateFinEvent("2025-10-22");
    incident.setAdresseIncident(new Adresse("Rue du Rhône 1", "1201 Genève", "1201", "Genève", "6621", "Suisse", "8100"));
    return incident;
  }

  @Test
  void shouldPassValidationWithAllRequiredFields() {
    FakeIncident incident = createValidIncident();
    assertDoesNotThrow(incident::champsObligatoireIncident);
  }

  @Test
  void shouldThrowWhenAdresseMissing() {
    FakeIncident incident = createValidIncident();
    incident.setAdresseIncident(null);

    Exception ex = assertThrows(ValidationMetierException.class, incident::champsObligatoireIncident);
    assertEquals("L’adresse de l’incident est obligatoire.", ex.getMessage());
  }
}
