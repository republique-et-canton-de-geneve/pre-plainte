package ch.ge.police.infrastructure.adapter.out;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.ge.police.core.domain.exception.S3AccessException;
import ch.ge.police.core.domain.exception.S3NotFoundException;
import ch.ge.police.core.domain.model.PrePlainte;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3JsonDraftAdapterTest {

  @Mock S3Client s3Client;
  @Mock ObjectMapper mapper;

  private S3JsonDraftAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new S3JsonDraftAdapter(s3Client, mapper);
    setField(adapter, "bucketName", "my-bucket");
  }

  @Test
  void sauvegarderBrouillon_putsJsonToS3_withExpectedRequestAndBody() throws Exception {
    PrePlainte prePlainte = mock(PrePlainte.class);
    String demandeId = "AEL-PPL-123";
    byte[] jsonBytes = "{\n  \"ok\": true\n}".getBytes(StandardCharsets.UTF_8);

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(prePlainte)).thenReturn(jsonBytes);

    ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    adapter.sauvegarderBrouillon(prePlainte, demandeId);

    verify(s3Client).putObject(reqCaptor.capture(), bodyCaptor.capture());

    PutObjectRequest req = reqCaptor.getValue();
    assertEquals("my-bucket", req.bucket());
    assertEquals("preplainte/draft/" + demandeId + ".json", req.key());
    assertEquals("application/json", req.contentType());

    RequestBody body = bodyCaptor.getValue();
    assertNotNull(body);
    byte[] sent = body.contentStreamProvider().newStream().readAllBytes();
    assertArrayEquals(jsonBytes, sent);
  }

  @Test
  void sauvegarderBrouillon_whenS3Exception_throwsS3AccessException() throws Exception {
    PrePlainte prePlainte = mock(PrePlainte.class);
    String demandeId = "AEL-PPL-123";

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(prePlainte)).thenReturn("{}".getBytes(StandardCharsets.UTF_8));

    doThrow(S3Exception.builder().statusCode(500).message("boom").build())
      .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    S3AccessException ex = assertThrows(
      S3AccessException.class,
      () -> adapter.sauvegarderBrouillon(prePlainte, demandeId)
    );

    assertTrue(ex.getMessage().contains("Erreur lors de la sauvegarde du brouillon sur S3"));
    assertNotNull(ex.getCause());
  }

  @Test
  void sauvegarderBrouillon_whenJsonSerializationFails_throwsS3AccessException() throws Exception {
    PrePlainte prePlainte = mock(PrePlainte.class);
    String demandeId = "AEL-PPL-123";

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(prePlainte)).thenThrow(new JsonProcessingException("bad json") {});

    S3AccessException ex = assertThrows(
      S3AccessException.class,
      () -> adapter.sauvegarderBrouillon(prePlainte, demandeId)
    );

    assertTrue(ex.getMessage().contains("Erreur lors de la sauvegarde du brouillon sur S3"));
    assertNotNull(ex.getCause());
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void chargerBrouillon_getsJsonFromS3_andMapsToPrePlainte() throws Exception {
    String demandeId = "AEL-PPL-123";
    PrePlainte expected = mock(PrePlainte.class);

    when(mapper.readValue(any(ResponseInputStream.class), eq(PrePlainte.class))).thenReturn(expected);
    byte[] bytes = "{\"x\":1}".getBytes(StandardCharsets.UTF_8);
    ResponseInputStream<GetObjectResponse> responseStream =
      new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(new ByteArrayInputStream(bytes))
      );

    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

    PrePlainte actual = adapter.chargerBrouillon(demandeId);

    assertSame(expected, actual);

    ArgumentCaptor<GetObjectRequest> reqCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(reqCaptor.capture());
    GetObjectRequest req = reqCaptor.getValue();
    assertEquals("my-bucket", req.bucket());
    assertEquals("preplainte/draft/" + demandeId + ".json", req.key());
  }

  @Test
  void chargerBrouillon_whenNoSuchKey_throwsS3NotFoundException() {
    String demandeId = "AEL-PPL-123";

    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(NoSuchKeyException.builder().message("not found").build());

    assertThrows(S3NotFoundException.class, () -> adapter.chargerBrouillon(demandeId));
  }

  @Test
  void chargerBrouillon_whenS3Exception404_throwsS3NotFoundException() {
    String demandeId = "AEL-PPL-123";

    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(S3Exception.builder().statusCode(404).message("not found").build());

    assertThrows(S3NotFoundException.class, () -> adapter.chargerBrouillon(demandeId));
  }

  @Test
  void chargerBrouillon_whenS3ExceptionNot404_throwsS3AccessException() {
    String demandeId = "AEL-PPL-123";

    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(S3Exception.builder().statusCode(500).message("boom").build());

    assertThrows(S3AccessException.class, () -> adapter.chargerBrouillon(demandeId));
  }

  @Test
  void chargerBrouillon_whenMapperFails_throwsS3AccessException() throws Exception {
    String demandeId = "AEL-PPL-123";

    byte[] bytes = "{\"x\":1}".getBytes(StandardCharsets.UTF_8);
    ResponseInputStream<GetObjectResponse> responseStream =
      new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(new ByteArrayInputStream(bytes))
      );
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

    when(mapper.readValue(any(ResponseInputStream.class), eq(PrePlainte.class)))
      .thenThrow(new RuntimeException("parse failed"));

    assertThrows(S3AccessException.class, () -> adapter.chargerBrouillon(demandeId));
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Unable to set field " + fieldName, e);
    }
  }
}
