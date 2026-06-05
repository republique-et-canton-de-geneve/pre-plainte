package ch.ge.police.infrastructure.ech051;

import java.util.ArrayList;
import java.util.List;

public final class MyAbiAdditionalInformationFormatter {

  static final String PREFIX = "Autre indications";
  private static final String SEPARATOR = "; ";

  private MyAbiAdditionalInformationFormatter() {
  }

  public static String format(List<String> items) {
    if (items == null || items.isEmpty()) {
      return null;
    }
    List<String> normalized = items.stream()
        .filter(item -> item != null && !item.isBlank())
        .map(String::trim)
        .toList();
    if (normalized.isEmpty()) {
      return null;
    }
    return PREFIX + SEPARATOR + String.join(SEPARATOR, normalized);
  }

  public static String labeledItem(String label, String value) {
    if (label == null || label.isBlank() || value == null || value.isBlank()) {
      return null;
    }
    return label.trim() + ": " + value.trim();
  }

  public static String booleanItem(String label, Boolean value) {
    if (value == null || !value) {
      return null;
    }
    return labeledItem(label, "oui");
  }

  public static List<String> builder() {
    return new ArrayList<>();
  }

  public static void addLabeled(List<String> items, String label, String value) {
    String item = labeledItem(label, value);
    if (item != null) {
      items.add(item);
    }
  }

  public static void addBoolean(List<String> items, String label, boolean value) {
    String item = booleanItem(label, value);
    if (item != null) {
      items.add(item);
    }
  }
}
