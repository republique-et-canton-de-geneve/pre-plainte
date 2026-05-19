package ch.ge.police.core.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DomainExceptionsTest {

  @Test
  void shouldCreateChallengeStorageException() {
    Throwable cause = new RuntimeException("root cause");

    ChallengeStorageException exception = new ChallengeStorageException("challenge error", cause);

    assertEquals("challenge error", exception.getMessage());
    assertEquals(cause, exception.getCause());
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void shouldCreateEmailNotSentException() {
    Exception cause = new Exception("mail error");

    EmailNotSentException exception = new EmailNotSentException("email not sent", cause);

    assertEquals("email not sent", exception.getMessage());
    assertEquals(cause, exception.getCause());
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void shouldCreateS3AccessException() {
    Exception cause = new Exception("s3 access error");

    S3AccessException exception = new S3AccessException("s3 access failed", cause);

    assertEquals("s3 access failed", exception.getMessage());
    assertEquals(cause, exception.getCause());
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void shouldCreateS3NotFoundException() {
    Exception cause = new Exception("s3 not found");

    S3NotFoundException exception = new S3NotFoundException("s3 object not found", cause);

    assertEquals("s3 object not found", exception.getMessage());
    assertEquals(cause, exception.getCause());
    assertInstanceOf(RuntimeException.class, exception);
  }
}
