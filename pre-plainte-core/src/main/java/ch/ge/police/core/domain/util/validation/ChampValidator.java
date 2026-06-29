package ch.ge.police.core.domain.util.validation;

import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;

import java.util.Collection;

public final class ChampValidator {

  private ChampValidator() {
  }

  public static void verifierLongueurMax(String value, int maxLength, String fieldName) {
    if (value != null && value.length() > maxLength) {
      throw new ValidationMetierException(
        String.format("Le champ '%s' dépasse la longueur maximale de %d caractères.", fieldName, maxLength)
      );
    }
  }

  public static void verifierChampObligatoire(Object value, String messageErreur) {
    if (value == null) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof String valueStr && valueStr.isBlank()) {
      throw new ValidationMetierException(messageErreur);
    }
    if (value instanceof RipolCode rc && !rc.hasCode()) {
      throw new ValidationMetierException(messageErreur);
    }
  }

  public static void verifierCollectionNonVide(Collection<?> collection, String messageErreur) {
    if (collection == null || collection.isEmpty()) {
      throw new ValidationMetierException(messageErreur);
    }
  }
}
