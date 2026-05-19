package ch.ge.police.core.domain.model.notification;

import ch.ge.police.core.application.service.NotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyServiceTest {

  private EmailSender emailSender;
  private NotifyService service;

  @BeforeEach
  void setUp() {
    emailSender = mock(EmailSender.class);
    service = new NotifyService(emailSender);
  }

  @Test
  void sendSimple_buildsEmailMessageAndDelegatesToSender() {
    service.sendSimple("from@test.ch", "to@test.ch", "Sujet", "Contenu");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender).send(captor.capture());

    EmailMessage message = captor.getValue();
    assertEquals("from@test.ch", message.from().value());
    assertEquals("to@test.ch", message.to().value());
    assertEquals("Sujet", message.subject());
    assertEquals("Contenu", message.textBody());
  }

  @ParameterizedTest
  @CsvSource({"CHALLENGE,FR,Votre code", "CHALLENGE,EN,Your code", "CHALLENGE,DE,Ihr Code", "CHALLENGE,PT,O seu código", "BROUILLON,FR,Enregistrement de votre brouillon", "BROUILLON,EN,Your draft has been saved", "BROUILLON,DE,Ihre Entwurfsspeicherung", "BROUILLON,PT,Registo do seu rascunho"})
  void resolveSubject_returnsExpectedSubject(TemplateEmail template, EmailLanguage language, String expected) {
    assertEquals(expected, service.resolveSubject(template, language));
  }

  @Test
  void buildEmailBody_loadsTemplateAndInjectsData() {
    try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class, (mock, context) -> {
      String template = "<html><body>${data}</body></html>";
      when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
    })) {

      String body = service.buildEmailBody("1234", TemplateEmail.CHALLENGE, EmailLanguage.FR);

      assertEquals("<html><body>1234</body></html>", body);
      assertEquals(1, mocked.constructed().size());
    }
  }

  @Test
  void buildEmailBody_replacesAllOccurrencesOfPlaceholder() {
    try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class, (mock, context) -> {
      String template = "${data} - ${data}";
      when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
    })) {

      String body = service.buildEmailBody("ABC", TemplateEmail.BROUILLON, EmailLanguage.EN);

      assertEquals("ABC - ABC", body);
      assertEquals(1, mocked.constructed().size());
    }
  }

  @Test
  void buildEmailBody_whenTemplateCannotBeRead_throwsIllegalStateException() {
    try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class, (mock, context) -> when(mock.getInputStream()).thenThrow(new IOException("boom")))) {

      IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.buildEmailBody("1234", TemplateEmail.CHALLENGE, EmailLanguage.DE));

      assertTrue(ex.getMessage().contains("Impossible de charger le template d'email: CHALLENGE"));
      assertInstanceOf(IOException.class, ex.getCause());
      assertEquals("boom", ex.getCause().getMessage());
      assertEquals(1, mocked.constructed().size());
    }
  }
}
