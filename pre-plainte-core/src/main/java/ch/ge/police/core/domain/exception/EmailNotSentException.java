package ch.ge.police.core.domain.exception;

public class EmailNotSentException extends RuntimeException {
  public EmailNotSentException(String message, Exception e) {
    super(message, e);
  }
}
