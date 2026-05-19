package ch.ge.police.infrastructure.mail;

import ch.ge.police.core.domain.exception.EmailNotSentException;
import ch.ge.police.core.domain.model.notification.EmailAddress;
import ch.ge.police.core.domain.model.notification.EmailMessage;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

  @Mock
  JavaMailSender mail;

  @Test
  void send_buildsMimeMessageAndSends_withInlineLogo() throws Exception {
    Session session = Session.getInstance(new Properties());
    MimeMessage mime = new MimeMessage(session);
    when(mail.createMimeMessage()).thenReturn(mime);

    SmtpEmailSender sender = new SmtpEmailSender(mail);

    EmailMessage message = new EmailMessage(
      new EmailAddress("no-reply@example.com"),
      new EmailAddress("user@example.com"),
      "Sujet test",
      "<html><body>Bonjour<img src='cid:logoPreplainte'/></body></html>"
    );
    sender.send(message);

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mail).send(captor.capture());
    MimeMessage sent = captor.getValue();
    sent.saveChanges();

    assertEquals("no-reply@example.com", ((InternetAddress) sent.getFrom()[0]).getAddress());
    assertEquals("user@example.com",
      ((InternetAddress) sent.getRecipients(MimeMessage.RecipientType.TO)[0]).getAddress());
    assertEquals("Sujet test", sent.getSubject());

    ScanResult res = new ScanResult();
    scan(sent, res);

    assertTrue(res.hasHtml, "HTML part not found");
    assertNotNull(res.html, "HTML content is null");
    assertTrue(res.html.contains("Bonjour"), "HTML content does not contain expected text");
    assertTrue(res.hasInlineLogoCid, "Inline logo with Content-ID 'logoPreplainte' not found");
  }

  @Test
  void send_whenMailSenderThrows_wrapsInEmailNotSentException() throws Exception {
    Session session = Session.getInstance(new Properties());
    MimeMessage mime = new MimeMessage(session);

    when(mail.createMimeMessage()).thenReturn(mime);
    doThrow(new RuntimeException("smtp down")).when(mail).send(any(MimeMessage.class));

    SmtpEmailSender sender = new SmtpEmailSender(mail);

    EmailMessage message = new EmailMessage(
      new EmailAddress("no-reply@example.com"),
      new EmailAddress("user@example.com"),
      "Sujet test",
      "<b>Hi</b>"
    );

    EmailNotSentException ex = assertThrows(EmailNotSentException.class, () -> sender.send(message));
    assertEquals("Email send failed", ex.getMessage());
    assertNotNull(ex.getCause());
    assertEquals("smtp down", ex.getCause().getMessage());
  }

  private static final class ScanResult {
    boolean hasHtml = false;
    String html = null;
    boolean hasInlineLogoCid = false;
  }

  private static void scan(Part part, ScanResult res) throws Exception {
    if (part.isMimeType("text/html")) {
      res.hasHtml = true;
      Object c = part.getContent();
      if (c instanceof String s) {
        res.html = s;
      }
    }
    String[] cids = part.getHeader("Content-ID");
    if (cids != null) {
      for (String cid : cids) {
        if (cid != null && cid.contains("logoPreplainte")) {
          res.hasInlineLogoCid = true;
        }
      }
    }
    if (part.isMimeType("multipart/*")) {
      Object content = part.getContent();
      if (content instanceof Multipart mp) {
        for (int i = 0; i < mp.getCount(); i++) {
          BodyPart bp = mp.getBodyPart(i);
          scan(bp, res);
        }
      }
    }
  }
}
