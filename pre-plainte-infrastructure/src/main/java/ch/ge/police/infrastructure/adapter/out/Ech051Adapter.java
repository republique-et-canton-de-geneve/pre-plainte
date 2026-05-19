package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.port.out.PrePlainteSubmissionPort;
import ch.ge.police.infrastructure.ech051.Ech051Builder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ech051Adapter implements PrePlainteSubmissionPort {

  private static final String ECH051_PREFIX = "preplainte/";
  private static final String CONTENT_TYPE_XML = "application/xml";
  private static final String EVENT_ECH051_EXPORT_SUCCESS = "ech051_export_success";
  private static final String EVENT_ECH051_EXPORT_FAILURE = "ech051_export_failure";
  private static final String TRACE_ID = "traceId";

  private final Ech051Builder builder;
  private final S3Client s3Client;

  @Value("${s3.bucket.name}")
  private String bucketName;

  @Value("${s3.endpoint}")
  private String s3Endpoint;

  private String buildKey() {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return ECH051_PREFIX + "preplainte-" + datePart + "-" + uuid + ".xml";
  }

  @Override
  public void soumettrePrePlainteVersNas(PrePlainte prePlainte) {
    String demandeId = prePlainte != null ? prePlainte.getDemandeId() : null;
    String traceId = MDC.get(TRACE_ID);
    String key = buildKey();

    try {
      String xml = builder.generateEch051Xml(prePlainte, true);

      PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(CONTENT_TYPE_XML)
        .build();

      s3Client.putObject(request, RequestBody.fromString(xml, StandardCharsets.UTF_8));

      log.info(
        "event={} traceId={} demandeId={} storage=s3 bucket={} key={} contentType={} endpoint={}",
        EVENT_ECH051_EXPORT_SUCCESS,
        traceId,
        demandeId,
        bucketName,
        key,
        CONTENT_TYPE_XML,
        s3Endpoint
      );
    } catch (S3Exception e) {
      log.error(
        "event={} traceId={} demandeId={} storage=s3 bucket={} key={} status={} errorCode={} errorMessage={}",
        EVENT_ECH051_EXPORT_FAILURE,
        traceId,
        demandeId,
        bucketName,
        key,
        e.statusCode(),
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
        e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
        e
      );
      throw new IllegalStateException("Impossible d'écrire le fichier eCH-0051 sur S3", e);
    } catch (Exception e) {
      log.error(
        "event={} traceId={} demandeId={} storage=s3 bucket={} key={} error={}",
        EVENT_ECH051_EXPORT_FAILURE,
        traceId,
        demandeId,
        bucketName,
        key,
        e.getMessage(),
        e
      );
      throw new IllegalStateException("Impossible d'écrire le fichier eCH-0051 sur S3", e);
    }
  }
}
