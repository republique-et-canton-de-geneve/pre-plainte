package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.demande.DemandeId;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemandeIdServiceTest {

  private static final int RANDOM_PART_LENGTH = 10;

  private final DemandeIdService service = new DemandeIdService();

  @Test
  void generateReturnsProperlyFormattedId() {
    DemandeId id = service.generate(TypeIncident.VOL);

    assertNotNull(id);
    assertTrue(id.value().startsWith("AEL-PPL-V-"));
    assertEquals( "AEL-PPL-V-".length() + RANDOM_PART_LENGTH, id.value().length());
  }

  @Test
  void generateReturnsProperlyFormattedIdWhenTypeIncidentIsNull() {
    DemandeId id = service.generate(null);

    assertNotNull(id);
    assertTrue(id.value().startsWith("AEL-PPL-"));
    assertEquals( "AEL-PPL-".length() + RANDOM_PART_LENGTH, id.value().length());
  }

  @Test
  void generateContainsRandomPart() {
    DemandeId id1 = service.generate(TypeIncident.CYBER);
    DemandeId id2 = service.generate(TypeIncident.CYBER);

    assertNotEquals(id1.value(), id2.value());
  }
}
