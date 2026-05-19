package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.exception.S3AccessException;
import ch.ge.police.core.domain.exception.S3NotFoundException;
import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.port.out.PrePlainteBrouillontPort;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class S3JsonDraftAdapter implements PrePlainteBrouillontPort {

  private static final String DRAFT_PREFIX = "preplainte/draft/";
  private static final int STATUS_CODE_NOT_FOUND = 404;
  private static final String CONTENT_TYPE_JSON = "application/json";
  private static final String TRACE_ID = "traceId";

  private final S3Client s3Client;
  private final ObjectMapper mapper;

  @Value("${s3.bucket.name}")
  private String bucketName;

  private String buildKey(String demandeId) {
    return DRAFT_PREFIX + demandeId + ".json";
  }

  @Override
  public void sauvegarderBrouillon(PrePlainte prePlainte, String demandeId) {
    String key = buildKey(demandeId);

    try {
      byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(prePlainte);

      PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(CONTENT_TYPE_JSON)
        .build();

      s3Client.putObject(request, RequestBody.fromBytes(json));

      log.info(
        "event=preplainte_draft_save_success traceId={} demandeId={} bucket={} key={} contentType={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key,
        CONTENT_TYPE_JSON
      );
    } catch (S3Exception e) {
      log.error(
        "event=preplainte_draft_save_failure traceId={} demandeId={} bucket={} key={} status={} errorCode={} errorMessage={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new S3AccessException("Erreur lors de la sauvegarde du brouillon sur S3", e);
    } catch (Exception e) {
      log.error(
        "event=preplainte_draft_save_failure traceId={} demandeId={} bucket={} key={} error={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key,
        e.getMessage(),
        e
      );
      throw new S3AccessException("Erreur lors de la sauvegarde du brouillon sur S3", e);
    }
  }

  @Override
  public PrePlainte chargerBrouillon(String demandeId) {
    String key = buildKey(demandeId);

    try {
      GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

      ResponseInputStream<?> s3Object = s3Client.getObject(request);
      PrePlainte prePlainte = mapper.readValue(s3Object, PrePlainte.class);

      log.info(
        "event=preplainte_draft_load_success traceId={} demandeId={} bucket={} key={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key
      );

      return prePlainte;
    } catch (NoSuchKeyException e) {
      log.info(
        "event=preplainte_draft_not_found traceId={} demandeId={} bucket={} key={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key
      );
      throw new S3NotFoundException("Aucun brouillon trouvé pour " + demandeId, e);
    } catch (S3Exception e) {
      if (e.statusCode() == STATUS_CODE_NOT_FOUND) {
        log.info(
          "event=preplainte_draft_not_found traceId={} demandeId={} bucket={} key={}",
          MDC.get(TRACE_ID),
          demandeId,
          bucketName,
          key
        );
        throw new S3NotFoundException("Aucun brouillon trouvé pour " + demandeId, e);
      }

      log.error(
        "event=preplainte_draft_load_failure traceId={} demandeId={} bucket={} key={} status={} errorCode={} errorMessage={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new S3AccessException("Erreur lors du chargement du brouillon depuis S3", e);
    } catch (Exception e) {
      log.error(
        "event=preplainte_draft_load_failure traceId={} demandeId={} bucket={} key={} error={}",
        MDC.get(TRACE_ID),
        demandeId,
        bucketName,
        key,
        e.getMessage(),
        e
      );
      throw new S3AccessException("Erreur lors du chargement du brouillon depuis S3", e);
    }
  }
}
