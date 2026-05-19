package ch.ge.police.core.domain.model.notification;

public record EmailMessage(
  EmailAddress from,
  EmailAddress to,
  String subject,
  String textBody
) {}
