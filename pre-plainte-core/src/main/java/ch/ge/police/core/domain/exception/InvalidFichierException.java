package ch.ge.police.core.domain.exception;

public class InvalidFichierException extends RuntimeException {

  public InvalidFichierException(String message) {
    super(message);
  }

  public InvalidFichierException(String message, Throwable cause) {
    super(message, cause);
  }
}
