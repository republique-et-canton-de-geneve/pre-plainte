package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.exception.S3AccessException;
import ch.ge.police.core.domain.exception.S3NotFoundException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@ConditionalOnProperty(name = "ripol.db.source", havingValue = "s3", matchIfMissing = true)
public class S3RipolDatabaseSource implements RipolDatabaseSource {

  private static final String TRACE_ID = "traceId";
  private static final int STATUS_CODE_NOT_FOUND = 404;

  private final S3Client s3Client;
  private final String bucketName;
  private final String key;

  public S3RipolDatabaseSource(
    S3Client s3Client,
    @Value("${s3.bucket.name}") String bucketName,
    @Value("${ripol.db.s3.key:preplaintes/ripol/dbppel3.sqlite}") String key
  ) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.key = key;
  }

  @Override
  public InputStream ouvrirFlux() {
    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();

    try {
      InputStream flux = s3Client.getObject(request);

      log.info(
        "event=ripol_db_download_started traceId={} bucket={} key={}",
        MDC.get(TRACE_ID),
        bucketName,
        key
      );

      return flux;
    } catch (NoSuchKeyException e) {
      throw introuvable(e);
    } catch (S3Exception e) {
      if (e.statusCode() == STATUS_CODE_NOT_FOUND) {
        throw introuvable(e);
      }

      log.error(
        "event=ripol_db_download_failure traceId={} bucket={} key={} status={} errorCode={} errorMessage={}",
        MDC.get(TRACE_ID),
        bucketName,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new S3AccessException("Erreur lors du téléchargement de la base RIPOL depuis S3", e);
    }
  }

  @Override
  public String description() {
    return "s3://" + bucketName + "/" + key;
  }

  private S3NotFoundException introuvable(S3Exception e) {
    log.error(
      "event=ripol_db_not_found traceId={} bucket={} key={}",
      MDC.get(TRACE_ID),
      bucketName,
      key
    );
    return new S3NotFoundException("Base RIPOL introuvable sur S3 : " + description(), e);
  }
}
