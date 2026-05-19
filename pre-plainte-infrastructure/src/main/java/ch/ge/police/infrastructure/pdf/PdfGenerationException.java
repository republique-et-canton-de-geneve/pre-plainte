package ch.ge.police.infrastructure.pdf;

public class PdfGenerationException extends RuntimeException {

  public PdfGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
