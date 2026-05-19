package ch.ge.police.infrastructure.mail;

import ch.ge.police.core.domain.exception.EmailNotSentException;
import ch.ge.police.core.domain.model.notification.EmailMessage;
import ch.ge.police.core.domain.model.notification.EmailSender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {

  private final JavaMailSender mail;

  public SmtpEmailSender(JavaMailSender mail) {
    this.mail = mail;
  }

  @Override
  public void send(EmailMessage message) {
    try {
      MimeMessage mime = mail.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

      helper.setFrom(message.from().toString());
      helper.setTo(message.to().toString());
      helper.setSubject(message.subject());
      helper.setText(message.textBody(), true);

      ClassPathResource logo = new ClassPathResource("mail/logo-dark.png");
      helper.addInline("logoPreplainte", logo, "image/png");

      mail.send(mime);
    } catch (Exception e) {
      throw new EmailNotSentException("Email send failed", e);
    }
  }
}
