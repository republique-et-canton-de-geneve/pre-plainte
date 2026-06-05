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
  void labeledItem_shouldReturnNullWhenBlank() {
    assertNull(MyAbiAdditionalInformationFormatter.labeledItem("Label", " "));
  }
}
