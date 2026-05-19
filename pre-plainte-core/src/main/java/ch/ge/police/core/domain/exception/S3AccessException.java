package ch.ge.police.core.domain.exception;

public class S3AccessException extends RuntimeException {
  public S3AccessException(String message, Exception e) {
    super(message, e);
  }
}
