package ch.ge.police.infrastructure.ech051;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyAbiAdditionalInformationFormatterTest {

  @Test
  void format_shouldPrefixWithAutreIndicationsAndUseSemicolonSeparator() {
    String result = MyAbiAdditionalInformationFormatter.format(List.of(
        "Reçu de caisse: Motif de l'absence de la quittance de l'achat"
    ));

    assertEquals(
        "Autre indications; Reçu de caisse: Motif de l'absence de la quittance de l'achat",
        result
    );
  }

  @Test
  void format_shouldReturnNullForEmptyOrNullInput() {
    assertNull(MyAbiAdditionalInformationFormatter.format(null));
    assertNull(MyAbiAdditionalInformationFormatter.format(List.of()));
    assertNull(MyAbiAdditionalInformationFormatter.format(List.of(" ", "")));
  }

  @Test
  void booleanItem_shouldReturnOuiWhenTrue() {
    assertEquals("Actif: oui", MyAbiAdditionalInformationFormatter.booleanItem("Actif", true));
    assertNull(MyAbiAdditionalInformationFormatter.booleanItem("Actif", false));
    assertNull(MyAbiAdditionalInformationFormatter.booleanItem("Actif", null));
  }

  @Test
  void addLabeledAndAddBoolean_shouldIgnoreBlankValues() {
    List<String> items = MyAbiAdditionalInformationFormatter.builder();
    MyAbiAdditionalInformationFormatter.addLabeled(items, "Label", " ");
    MyAbiAdditionalInformationFormatter.addBoolean(items, "Flag", false);

    assertEquals(0, items.size());
  }

  @Test
  void labeledItem_shouldReturnNullWhenBlank() {
    assertNull(MyAbiAdditionalInformationFormatter.labeledItem("Label", " "));
  }
}
