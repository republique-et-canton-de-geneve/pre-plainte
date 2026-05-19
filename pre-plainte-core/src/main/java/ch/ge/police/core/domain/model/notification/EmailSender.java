package ch.ge.police.core.domain.model.notification;

public interface EmailSender {
  void send(EmailMessage message);
}
