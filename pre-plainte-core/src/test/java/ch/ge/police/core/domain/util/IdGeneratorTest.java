package ch.ge.police.core.domain.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

  private static final int RANDOM_PART_LENGTH = 10;

  @Test
  void randomAlphanumericHasCorrectLength() {
    String id = IdGenerator.randomAlphanumeric(RANDOM_PART_LENGTH);
    assertEquals(RANDOM_PART_LENGTH, id.length());
  }

  @Test
  void randomAlphanumericContainsOnlyAllowedChars() {
    String id = IdGenerator.randomAlphanumeric(RANDOM_PART_LENGTH);
    assertTrue(id.matches("[\\p{Lu}\\p{Nd}]+"));
  }

  @Test
  void randomAlphanumericGeneratesDifferentValues() {
    String id1 = IdGenerator.randomAlphanumeric(RANDOM_PART_LENGTH);
    String id2 = IdGenerator.randomAlphanumeric(RANDOM_PART_LENGTH);
    assertNotEquals(id1, id2);
  }
}
