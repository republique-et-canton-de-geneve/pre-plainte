package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.application.service.NotifyService;
import ch.ge.police.core.domain.exception.ChallengeStorageException;
import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.domain.model.notification.TemplateEmail;
import ch.ge.police.core.port.out.EmailChallengeStoragePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ChallengeEmailAdapter implements EmailChallengeStoragePort {

  private static final String PREFIX = "preplainte/email-challenge/";
  private static final int STATUS_CODE_NOT_FOUND = 404;
  private static final String CONTENT_TYPE_JSON = "application/json";
  private static final String TRACE_ID = "traceId";
  private static final int SMALL_EMAIL_LIMIT = 2;
  private static final int EMAIL_PARTS_NUMBER = 2;

  private final S3Client s3Client;
  private final ObjectMapper mapper;
  private final NotifyService notifyService;

  @Value("${s3.bucket.name}")
  private String bucketName;

  @Value("${spring.mail.from}")
  private String from;

  private String buildKey(String key) {
    return PREFIX + key + ".json";
  }

  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }
    String[] parts = email.split("@", EMAIL_PARTS_NUMBER);
    String localPart = parts[0];
    String domain = parts[1];
    String maskedLocalPart = (localPart.length() <= SMALL_EMAIL_LIMIT) ? ("***") : (localPart.substring(0, SMALL_EMAIL_LIMIT) + "***");
    return maskedLocalPart + "@" + domain;
  }

  @Override
  public Optional<EmailChallenge> charger(String key) {
    String s3Key = buildKey(key);

    try {
      GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(s3Key)
        .build();

      ResponseInputStream<?> s3Object = s3Client.getObject(request);
      EmailChallenge challenge = mapper.readValue(s3Object, EmailChallenge.class);

      log.info(
        "event=email_challenge_load_success traceId={} bucket={} key={} challengeKey={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key
      );

      return Optional.of(challenge);
    } catch (NoSuchKeyException e) {
      log.info(
        "event=email_challenge_not_found traceId={} bucket={} key={} challengeKey={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key
      );
      return Optional.empty();
    } catch (S3Exception e) {
      if (e.statusCode() == STATUS_CODE_NOT_FOUND) {
        log.info(
          "event=email_challenge_not_found traceId={} bucket={} key={} challengeKey={}",
          MDC.get(TRACE_ID),
          bucketName,
          s3Key,
          key
        );
        return Optional.empty();
      }

      log.error(
        "event=email_challenge_load_failure traceId={} bucket={} key={} challengeKey={} status={} errorCode={} errorMessage={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new ChallengeStorageException("Erreur lors du chargement du challenge depuis S3", e);
    } catch (Exception e) {
      log.error(
        "event=email_challenge_load_failure traceId={} bucket={} key={} challengeKey={} error={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key,
        e.getMessage(),
        e
      );
      throw new ChallengeStorageException("Erreur lors du chargement du challenge depuis S3", e);
    }
  }

  @Override
  public void sauvegarder(String key, EmailChallenge challenge) {
    String s3Key = buildKey(key);

    try {
      byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(challenge);

      PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(s3Key)
        .contentType(CONTENT_TYPE_JSON)
        .build();

      s3Client.putObject(request, RequestBody.fromBytes(json));

      log.info(
        "event=email_challenge_save_success traceId={} bucket={} key={} challengeKey={} contentType={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key,
        CONTENT_TYPE_JSON
      );
    } catch (S3Exception e) {
      log.error(
        "event=email_challenge_save_failure traceId={} bucket={} key={} challengeKey={} status={} errorCode={} errorMessage={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new ChallengeStorageException("Erreur lors de la sauvegarde du challenge sur S3", e);
    } catch (Exception e) {
      log.error(
        "event=email_challenge_save_failure traceId={} bucket={} key={} challengeKey={} error={}",
        MDC.get(TRACE_ID),
        bucketName,
        s3Key,
        key,
        e.getMessage(),
        e
      );
      throw new ChallengeStorageException("Erreur lors de la sauvegarde du challenge sur S3", e);
    }
  }

  @Override
  public void envoyerCode(String email, String code, EmailLanguage language) {
    try {
      String subject = notifyService.resolveSubject(TemplateEmail.CHALLENGE, language);
      String body = notifyService.buildEmailBody(code, TemplateEmail.CHALLENGE, language);
      notifyService.sendSimple(from, email, subject, body);

      log.info(
        "event=email_challenge_send_success traceId={} recipient={}",
        MDC.get(TRACE_ID),
        maskEmail(email)
      );
    } catch (Exception e) {
      log.error(
        "event=email_challenge_send_failure traceId={} recipient={} error={}",
        MDC.get(TRACE_ID),
        maskEmail(email),
        e.getMessage(),
        e
      );
      throw e;
    }
  }
}
