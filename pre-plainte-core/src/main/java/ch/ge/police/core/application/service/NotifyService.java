package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.notification.EmailAddress;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.domain.model.notification.EmailMessage;
import ch.ge.police.core.domain.model.notification.EmailSender;
import ch.ge.police.core.domain.model.notification.TemplateEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class NotifyService {
  private final EmailSender emailSender;

  public NotifyService(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  public void sendSimple(String from, String to, String subject, String body) {
    var msg = new EmailMessage(new EmailAddress(from), new EmailAddress(to), subject, body);
    emailSender.send(msg);
  }

  public String buildEmailBody(String data, TemplateEmail type, EmailLanguage language) {
    try {
      ClassPathResource resource = new ClassPathResource(type.templatePath(language));
      String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      return template.replace("${data}", data);
    } catch (IOException e) {
      log.error("Erreur de chargement du template d'email: {}", type, e);
      throw new IllegalStateException("Impossible de charger le template d'email: " + type, e);
    }
  }

  public String resolveSubject(TemplateEmail template, EmailLanguage language) {
    return switch (template) {
      case CHALLENGE -> resolveChallengeSubject(language);
      case BROUILLON -> resolveDraftSubject(language);
    };
  }

  private String resolveChallengeSubject(EmailLanguage language) {
    return switch (language) {
      case FR -> "Votre code";
      case EN -> "Your code";
      case DE -> "Ihr Code";
      case PT -> "O seu código";
    };
  }

  private String resolveDraftSubject(EmailLanguage language) {
    return switch (language) {
      case FR -> "Enregistrement de votre brouillon";
      case EN -> "Your draft has been saved";
      case DE -> "Ihre Entwurfsspeicherung";
      case PT -> "Registo do seu rascunho";
    };
  }
}
