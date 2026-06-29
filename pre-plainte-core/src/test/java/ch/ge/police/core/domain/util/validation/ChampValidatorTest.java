package ch.ge.police.core.domain.util.validation;

import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChampValidatorTest {

  @Test
  void should_throw_when_string_exceeds_max_length() {
    ValidationMetierException ex = assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierLongueurMax("abcdef", 3, "champ")
    );

    assertTrue(ex.getMessage().contains("dépasse la longueur maximale"));
  }

  @Test
  void should_not_throw_when_null_value() {
    assertDoesNotThrow(() ->
      ChampValidator.verifierLongueurMax(null, 3, "champ")
    );
  }

  @Test
  void should_not_throw_when_length_ok() {
    assertDoesNotThrow(() ->
      ChampValidator.verifierLongueurMax("abc", 3, "champ")
    );
  }

  @Test
  void should_throw_when_value_is_null() {
    assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierChampObligatoire(null, "obligatoire")
    );
  }

  @Test
  void should_throw_when_string_is_blank() {
    assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierChampObligatoire("   ", "obligatoire")
    );
  }

  @Test
  void should_throw_when_ripolcode_has_no_code() {
    RipolCode code = new RipolCode("", "label");

    assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierChampObligatoire(code, "obligatoire")
    );
  }

  @Test
  void should_not_throw_when_valid_string() {
    assertDoesNotThrow(() ->
      ChampValidator.verifierChampObligatoire("test", "obligatoire")
    );
  }

  @Test
  void should_not_throw_when_valid_ripolcode() {
    RipolCode code = new RipolCode("123", "label");

    assertDoesNotThrow(() ->
      ChampValidator.verifierChampObligatoire(code, "obligatoire")
    );
  }

  @Test
  void should_throw_when_collection_is_null() {
    assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierCollectionNonVide(null, "vide")
    );
  }

  @Test
  void should_throw_when_collection_is_empty() {
    List<String> emptyList = List.of();
    assertThrows(
      ValidationMetierException.class,
      () -> ChampValidator.verifierCollectionNonVide(emptyList, "vide")
    );
  }
  @Test
  void should_not_throw_when_collection_has_elements() {
    assertDoesNotThrow(() ->
      ChampValidator.verifierCollectionNonVide(List.of("a"), "vide")
    );
  }
}
