package ch.ge.police.core.domain.model.notification;

import java.util.Locale;

public enum EmailLanguage {
  FR("fr"),
  EN("en"),
  DE("de"),
  PT("pt");

  private final String code;

  EmailLanguage(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static EmailLanguage fromCode(String code) {
    if (code == null || code.isBlank()) {
      return FR;
    }

    return switch (code.toLowerCase(Locale.FRENCH)) {
      case "en" -> EN;
      case "de" -> DE;
      case "pt" -> PT;
      default -> FR;
    };
  }

  public static EmailLanguage fromLocale(Locale locale) {
    if (locale == null) {
      return FR;
    }
    return fromCode(locale.getLanguage());
  }
}
