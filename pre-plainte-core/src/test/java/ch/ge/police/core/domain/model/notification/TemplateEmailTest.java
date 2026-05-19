package ch.ge.police.core.domain.model.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEmailTest {

  @Test
  void shouldReturnClasspathForBrouillon() {
    assertEquals("mail/brouillon/template_fr.html", TemplateEmail.BROUILLON.templatePath(EmailLanguage.fromCode("fr")));
  }

  @Test
  void shouldReturnClasspathForChallenge() {
    assertEquals("mail/challenge/template_fr.html", TemplateEmail.CHALLENGE.templatePath(EmailLanguage.fromCode("fr")));
  }

  @Test
  void shouldContainExpectedEnumValues() {
    assertEquals(2, TemplateEmail.values().length);
    assertEquals(TemplateEmail.BROUILLON, TemplateEmail.valueOf("BROUILLON"));
    assertEquals(TemplateEmail.CHALLENGE, TemplateEmail.valueOf("CHALLENGE"));
  }
}
