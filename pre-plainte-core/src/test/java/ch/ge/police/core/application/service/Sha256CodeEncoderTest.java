package ch.ge.police.core.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sha256CodeEncoderTest {

  private final Sha256CodeEncoder encoder = new Sha256CodeEncoder();

  @Test
  void shouldVerifyKnownSha256Digest() {
    String encoded = "sha256$00000000000000000000000000000000$"
      + "bcfa9d35208eaff87d3d179ed05636b0b659157926158e3c645c1605a0c7d43f";
    assertTrue(encoder.matches("123456", encoded));
    assertFalse(encoder.matches("123457", encoded));
  }

  @Test
  void encodedCodeShouldBeVerifiableByAnotherInstance() {
    String encoded = encoder.encode("123456");
    assertTrue(encoded.matches("sha256\\$[0-9a-f]{32}\\$[0-9a-f]{64}"));
    assertTrue(new Sha256CodeEncoder().matches("123456", encoded));
    assertFalse(encoder.matches("654321", encoded));
    assertFalse(encoder.matches(null, encoded));
    assertFalse(encoder.matches(" 123456", encoded));
  }

  @Test
  void sameCodeShouldHaveDifferentSalts() {
    String first = encoder.encode("123456");
    String second = encoder.encode("123456");
    assertNotEquals(first, second);
    assertTrue(encoder.matches("123456", first));
    assertTrue(encoder.matches("123456", second));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "hash", "$2a$10$legacy", "sha256$00$00", "sha256$invalid$invalid"})
  void malformedOrLegacyHashShouldNotMatch(String encoded) {
    assertFalse(encoder.matches("123456", encoded));
  }
}
