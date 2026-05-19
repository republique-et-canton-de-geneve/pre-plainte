package ch.ge.police.core.domain.exception;

public class S3NotFoundException extends RuntimeException {
  public S3NotFoundException(String message, Exception e) {
    super(message, e);
  }
}
