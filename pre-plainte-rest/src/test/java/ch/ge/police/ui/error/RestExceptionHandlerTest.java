package ch.ge.police.ui.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.ge.police.core.application.service.EmailChallengeService;
import ch.ge.police.core.domain.exception.InvalidFichierException;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestExceptionHandlerTest {

  private static final String MESSAGE_KEY = "message";
  private static final String STATUS_KEY = "status";
  private static final String ERROR_CODE_KEY = "errorCode";

  private RestExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new RestExceptionHandler();
  }

  @Test
  void handleInvalidFichier_returnsBadRequestWithoutExceptionMessage() {
    ResponseEntity<Map<String, Object>> r =
        handler.handleInvalidFichier(new InvalidFichierException("fichier"));

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(r.getBody()).containsEntry(STATUS_KEY, HttpStatus.BAD_REQUEST.value());
    assertThat(r.getBody()).doesNotContainKey(MESSAGE_KEY);
  }

  @Test
  void handleValidationMetier_returnsBadRequestWithoutExceptionMessage() {
    ResponseEntity<Map<String, Object>> r =
        handler.handleValidationMetier(new ValidationMetierException("metier"));

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(r.getBody()).containsEntry(STATUS_KEY, HttpStatus.BAD_REQUEST.value());
    assertThat(r.getBody()).doesNotContainKey(MESSAGE_KEY);
  }

  @Test
  void handleEmailChallengeNotFound_returnsNotFoundWithCodeWithoutExceptionMessage() {
    ResponseEntity<Map<String, Object>> r = handler.handleEmailChallengeNotFound(
        new EmailChallengeService.NotFoundException("absent"));

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(r.getBody()).containsEntry(STATUS_KEY, HttpStatus.NOT_FOUND.value());
    assertThat(r.getBody()).containsEntry(ERROR_CODE_KEY, "EMAIL_CHALLENGE_NOT_FOUND");
    assertThat(r.getBody()).doesNotContainKey(MESSAGE_KEY);
  }

  @Test
  void handleEmailChallengeTooMany_returns429WithCodeWithoutExceptionMessage() {
    ResponseEntity<Map<String, Object>> r = handler.handleEmailChallengeTooMany(
        new EmailChallengeService.TooManyRequestsException("attendre"));

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    assertThat(r.getBody()).containsEntry(STATUS_KEY, HttpStatus.TOO_MANY_REQUESTS.value());
    assertThat(r.getBody()).containsEntry(ERROR_CODE_KEY, "EMAIL_CHALLENGE_TOO_MANY_REQUESTS");
    assertThat(r.getBody()).doesNotContainKey(MESSAGE_KEY);
  }

  @Test
  void handleUnhandled_returnsInternalServerErrorWithErrorIdWithoutExceptionMessage() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/x");

    ResponseEntity<Map<String, Object>> r =
        handler.handleUnhandled(new IllegalStateException("boom"), request);

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(r.getBody()).containsKeys(STATUS_KEY, "errorId");
    assertThat(r.getBody()).containsEntry(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(r.getBody()).doesNotContainKey(MESSAGE_KEY);
  }
}
