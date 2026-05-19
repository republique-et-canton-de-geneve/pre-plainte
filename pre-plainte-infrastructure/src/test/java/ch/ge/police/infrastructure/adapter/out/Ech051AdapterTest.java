package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.infrastructure.ech051.Ech051Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Ech051AdapterTest {

  private static final String BUCKET_NAME = "my-bucket";
  private static final String S3_ENDPOINT = "https://s3.example.test";
  private static final String CONTENT_TYPE_XML = "application/xml";
  private static final String ERROR_MESSAGE = "Impossible d'écrire le fichier eCH-0051 sur S3";

  @Mock
  Ech051Builder builder;

  @Mock
  S3Client s3Client;

  @Test
  void soumettrePrePlainteVersNas_putsXmlToS3_withExpectedKeyAndMetadata() {
    Ech051Adapter adapter = newAdapter();

    PrePlainte prePlainte = mock(PrePlainte.class);
    String xml = "<root>ok</root>";
    when(builder.generateEch051Xml(prePlainte, true)).thenReturn(xml);

    ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    adapter.soumettrePrePlainteVersNas(prePlainte);

    verify(builder).generateEch051Xml(prePlainte, true);
    verify(s3Client).putObject(reqCaptor.capture(), bodyCaptor.capture());

    PutObjectRequest req = reqCaptor.getValue();
    assertEquals(BUCKET_NAME, req.bucket());
    assertEquals(CONTENT_TYPE_XML, req.contentType());

    String key = req.key();
    assertNotNull(key);

    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    assertTrue(
      key.matches("^preplainte/preplainte-" + today + "-[0-9a-fA-F]{32}\\.xml$"),
      "Unexpected key: " + key
    );

    assertNotNull(bodyCaptor.getValue());
    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void soumettrePrePlainteVersNas_shouldSupportNullPrePlainte() {
    Ech051Adapter adapter = newAdapter();
    when(builder.generateEch051Xml(null, true)).thenReturn("<root>ok</root>");

    ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    adapter.soumettrePrePlainteVersNas(null);

    verify(builder).generateEch051Xml(null, true);
    verify(s3Client).putObject(reqCaptor.capture(), bodyCaptor.capture());

    PutObjectRequest req = reqCaptor.getValue();
    assertEquals(BUCKET_NAME, req.bucket());
    assertEquals(CONTENT_TYPE_XML, req.contentType());
    assertNotNull(req.key());
    assertNotNull(bodyCaptor.getValue());
  }

  @Test
  void soumettrePrePlainteVersNas_wrapsGenericExceptionIntoIllegalStateException() {
    Ech051Adapter adapter = newAdapter();

    PrePlainte prePlainte = mock(PrePlainte.class);
    when(builder.generateEch051Xml(prePlainte, true)).thenReturn("<root/>");

    doThrow(new RuntimeException("boom"))
      .when(s3Client)
      .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    IllegalStateException ex = assertThrows(
      IllegalStateException.class,
      () -> adapter.soumettrePrePlainteVersNas(prePlainte)
    );

    assertEquals(ERROR_MESSAGE, ex.getMessage());
    assertNotNull(ex.getCause());
    assertEquals("boom", ex.getCause().getMessage());
  }

  @Test
  void soumettrePrePlainteVersNas_wrapsS3ExceptionWithAwsErrorDetailsIntoIllegalStateException() {
    Ech051Adapter adapter = newAdapter();

    PrePlainte prePlainte = mock(PrePlainte.class);
    when(builder.generateEch051Xml(prePlainte, true)).thenReturn("<root/>");

    AwsErrorDetails awsErrorDetails = AwsErrorDetails.builder()
      .errorCode("AccessDenied")
      .errorMessage("Access denied")
      .build();

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
      .statusCode(403)
      .awsErrorDetails(awsErrorDetails)
      .message("S3 failure")
      .build();

    doThrow(s3Exception)
      .when(s3Client)
      .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    IllegalStateException ex = assertThrows(
      IllegalStateException.class,
      () -> adapter.soumettrePrePlainteVersNas(prePlainte)
    );

    assertEquals(ERROR_MESSAGE, ex.getMessage());
    assertEquals(s3Exception, ex.getCause());
    assertEquals(403, ((S3Exception) ex.getCause()).statusCode());
    assertNotNull(((S3Exception) ex.getCause()).awsErrorDetails());
    assertEquals("AccessDenied", ((S3Exception) ex.getCause()).awsErrorDetails().errorCode());
    assertEquals("Access denied", ((S3Exception) ex.getCause()).awsErrorDetails().errorMessage());
  }

  @Test
  void soumettrePrePlainteVersNas_wrapsS3ExceptionWithoutAwsErrorDetailsIntoIllegalStateException() {
    Ech051Adapter adapter = newAdapter();

    PrePlainte prePlainte = mock(PrePlainte.class);
    when(builder.generateEch051Xml(prePlainte, true)).thenReturn("<root/>");

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
      .statusCode(500)
      .message("Internal S3 error")
      .build();

    doThrow(s3Exception)
      .when(s3Client)
      .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    IllegalStateException ex = assertThrows(
      IllegalStateException.class,
      () -> adapter.soumettrePrePlainteVersNas(prePlainte)
    );

    assertEquals(ERROR_MESSAGE, ex.getMessage());
    assertEquals(s3Exception, ex.getCause());
    assertEquals(500, ((S3Exception) ex.getCause()).statusCode());
    assertNull(((S3Exception) ex.getCause()).awsErrorDetails());
  }

  private Ech051Adapter newAdapter() {
    Ech051Adapter adapter = new Ech051Adapter(builder, s3Client);
    ReflectionTestUtils.setField(adapter, "bucketName", BUCKET_NAME);
    ReflectionTestUtils.setField(adapter, "s3Endpoint", S3_ENDPOINT);
    return adapter;
  }
}
