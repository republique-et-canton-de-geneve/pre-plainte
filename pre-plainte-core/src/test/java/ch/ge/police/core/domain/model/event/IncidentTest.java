package ch.ge.police.core.domain.model.event;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTest {

  private Incident incident;

  @BeforeEach
  void setUp() {
    DommageMateriel dommage = new DommageMateriel();
    dommage.setDateDebutEvent("2025-10-21");
    dommage.setDateFinEvent("2025-10-21");
    dommage.setAdresseIncident(new Adresse("Rue du Rhône 10", "10", "1204", "Genève", null, "Suisse", null));
    dommage.setTypeDommage(TypeDommage.DOMMAGE_VEHICULE);
    dommage.setMontantEstime(1000.0);
    dommage.setDevise("CHF");
    dommage.setNaturesDommage(List.of(NatureDommage.DEGRADATIONS));
    dommage.setDescription("Rayures sur la porte de voiture");
    dommage.setConstatPresent(false);

    incident = Incident.of(dommage);
  }

  @Test
  void shouldSetTypeIncidentAutomaticallyWhenDetailsProvided() {
    assertEquals(TypeIncident.DOMMAGE, incident.getTypeIncident(),
      "Le type d'incident devrait provenir du getter de la sous-classe");
  }

  @Test
  void shouldThrowWhenNoIncidentDetailsProvided() {
    Exception ex = assertThrows(ValidationMetierException.class, () -> Incident.of(null));
    assertEquals("Les détails de l’incident ne peuvent pas être nuls.", ex.getMessage());
  }

  @Test
  void shouldPassValidationWithValidDetails() {
    assertDoesNotThrow(incident::validate, "Une pré-plainte complète ne devrait pas lever d'exception");
  }

  @Test
  void shouldThrowIfIncidentDetailsAreIncomplete() {
    DommageMateriel invalid = (DommageMateriel) incident.getDetails();
    invalid.setAdresseIncident(null);
    incident = Incident.of(invalid);

    Exception ex = assertThrows(ValidationMetierException.class, incident::validate);
    assertTrue(ex.getMessage().contains("adresse"),
      "Le message d’erreur devrait mentionner que l’adresse est obligatoire");
  }
}
