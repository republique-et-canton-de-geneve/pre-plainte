package ch.ge.police.ui.error;

import ch.ge.police.core.application.service.EmailChallengeService;
import ch.ge.police.core.domain.exception.InvalidFichierException;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

  private static final String RESPONSE_BODY_STATUS_KEY = "status";
  private static final String RESPONSE_BODY_ERROR_CODE_KEY = "errorCode";

  @ExceptionHandler(InvalidFichierException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidFichier(InvalidFichierException ex) {
    logHandledException(HttpStatus.BAD_REQUEST, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(ValidationMetierException.class)
  public ResponseEntity<Map<String, Object>> handleValidationMetier(ValidationMetierException ex) {
    logHandledException(HttpStatus.BAD_REQUEST, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(EmailChallengeService.NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEmailChallengeNotFound(EmailChallengeService.NotFoundException ex) {
    logHandledException(HttpStatus.NOT_FOUND, ex);
    Map<String, Object> body = buildBody(HttpStatus.NOT_FOUND);
    body.put(RESPONSE_BODY_ERROR_CODE_KEY, "EMAIL_CHALLENGE_NOT_FOUND");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(EmailChallengeService.TooManyRequestsException.class)
  public ResponseEntity<Map<String, Object>> handleEmailChallengeTooMany(
      EmailChallengeService.TooManyRequestsException ex) {
    logHandledException(HttpStatus.TOO_MANY_REQUESTS, ex);
    Map<String, Object> body = buildBody(HttpStatus.TOO_MANY_REQUESTS);
    body.put(RESPONSE_BODY_ERROR_CODE_KEY, "EMAIL_CHALLENGE_TOO_MANY_REQUESTS");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnhandled(Exception ex, HttpServletRequest request) {
    String errorId = UUID.randomUUID().toString();
    log.error(
        "event=unhandled_exception errorId={} status={} method={} exceptionType={}",
        errorId,
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        request.getMethod(),
        ex.getClass().getName());
    Map<String, Object> body = buildBody(HttpStatus.INTERNAL_SERVER_ERROR);
    body.put("errorId", errorId);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private Map<String, Object> buildBody(HttpStatus status) {
    Map<String, Object> body = new HashMap<>();
    body.put(RESPONSE_BODY_STATUS_KEY, status.value());
    return body;
  }

  private void logHandledException(HttpStatus status, Exception ex) {
    log.warn(
        "event=handled_exception status={} exceptionType={}",
        status.value(),
        ex.getClass().getName());
  }
}
