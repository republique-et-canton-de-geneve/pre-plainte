package ch.ge.police.infrastructure.s3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.ge.police.core.application.service.NotifyService;
import ch.ge.police.core.domain.exception.ChallengeStorageException;
import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.domain.model.notification.TemplateEmail;
import ch.ge.police.infrastructure.adapter.out.S3ChallengeEmailAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3ChallengeEmailAdapterTest {

  @Mock
  S3Client s3Client;
  @Mock
  ObjectMapper mapper;
  @Mock
  NotifyService notifyService;

  private S3ChallengeEmailAdapter adapter;

  @BeforeEach
  void setup() {
    adapter = new S3ChallengeEmailAdapter(s3Client, mapper, notifyService);
    setPrivateField(adapter, "bucketName", "my-bucket");
    setPrivateField(adapter, "from", "noreply@test.ch");
  }

  @Test
  void charger_ok_returnsOptionalOfChallenge_andBuildsCorrectKey() throws Exception {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.parse("2026-02-26T10:00:00Z")).expiresAt(Instant.parse("2026-02-26T10:10:00Z")).attempts(1).verified(false).build();

    @SuppressWarnings("unchecked") ResponseInputStream<GetObjectResponse> s3Stream = mock(ResponseInputStream.class);

    ArgumentCaptor<GetObjectRequest> reqCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
    when(s3Client.getObject(reqCaptor.capture())).thenReturn(s3Stream);
    when(mapper.readValue(s3Stream, EmailChallenge.class)).thenReturn(challenge);

    Optional<EmailChallenge> result = adapter.charger("abc");

    assertTrue(result.isPresent());
    assertEquals("user@test.ch", result.get().getEmail());

    GetObjectRequest req = reqCaptor.getValue();
    assertEquals("my-bucket", req.bucket());
    assertEquals("preplainte/email-challenge/abc.json", req.key());
  }

  @Test
  void charger_noSuchKey_returnsEmpty() {
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().message("no such key").build());

    Optional<EmailChallenge> result = adapter.charger("missing");

    assertTrue(result.isEmpty());
  }

  @Test
  void charger_s3Exception404_returnsEmpty() {
    S3Exception notFound = (S3Exception) S3Exception.builder().statusCode(404).message("not found").build();
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(notFound);

    Optional<EmailChallenge> result = adapter.charger("missing");

    assertTrue(result.isEmpty());
  }

  @Test
  void charger_s3ExceptionNon404_throwsChallengeStorageException() {
    S3Exception boom = (S3Exception) S3Exception.builder().statusCode(500).message("boom").build();
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(boom);

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.charger("abc"));
    assertTrue(ex.getMessage().contains("Erreur lors du chargement du challenge depuis S3"));
    assertSame(boom, ex.getCause());
  }

  @Test
  void charger_genericException_throwsChallengeStorageException() {
    RuntimeException boom = new RuntimeException("boom");
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(boom);

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.charger("abc"));
    assertTrue(ex.getMessage().contains("Erreur lors du chargement du challenge depuis S3"));
    assertSame(boom, ex.getCause());
  }

  @Test
  void sauvegarder_ok_putsObjectWithCorrectRequest() throws Exception {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.parse("2026-02-26T10:00:00Z")).expiresAt(Instant.parse("2026-02-26T10:10:00Z")).attempts(0).verified(false).build();

    byte[] json = "{\"a\":1}\n".getBytes(StandardCharsets.UTF_8);

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(challenge)).thenReturn(json);

    ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    adapter.sauvegarder("abc", challenge);

    verify(s3Client).putObject(reqCaptor.capture(), bodyCaptor.capture());

    PutObjectRequest req = reqCaptor.getValue();
    assertEquals("my-bucket", req.bucket());
    assertEquals("preplainte/email-challenge/abc.json", req.key());
    assertEquals("application/json", req.contentType());

    assertNotNull(bodyCaptor.getValue());

    verify(mapper).writerWithDefaultPrettyPrinter();
    verify(writer).writeValueAsBytes(challenge);
  }

  @Test
  void sauvegarder_whenPutFails_throwsChallengeStorageException() throws Exception {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.parse("2026-02-26T10:00:00Z")).expiresAt(Instant.parse("2026-02-26T10:10:00Z")).attempts(0).verified(false).build();

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(challenge)).thenReturn("{}".getBytes(StandardCharsets.UTF_8));

    S3Exception boom = (S3Exception) S3Exception.builder().statusCode(500).message("boom").build();
    doThrow(boom).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.sauvegarder("abc", challenge));
    assertTrue(ex.getMessage().contains("Erreur lors de la sauvegarde du challenge sur S3"));
    assertSame(boom, ex.getCause());
  }

  @Test
  void envoyerCode_buildsBodyAndSendsEmail() {
    when(notifyService.resolveSubject(TemplateEmail.CHALLENGE, EmailLanguage.FR)).thenReturn("Votre code");
    when(notifyService.buildEmailBody("1234", TemplateEmail.CHALLENGE, EmailLanguage.FR)).thenReturn("BODY");

    adapter.envoyerCode("user@test.ch", "1234", EmailLanguage.FR);

    verify(notifyService).resolveSubject(TemplateEmail.CHALLENGE, EmailLanguage.FR);
    verify(notifyService).buildEmailBody("1234", TemplateEmail.CHALLENGE, EmailLanguage.FR);
    verify(notifyService).sendSimple("noreply@test.ch", "user@test.ch", "Votre code", "BODY");
    verifyNoMoreInteractions(notifyService);
  }

  @Test
  void charger_whenMapperFails_throwsChallengeStorageException() throws Exception {
    @SuppressWarnings("unchecked") ResponseInputStream<GetObjectResponse> s3Stream = mock(ResponseInputStream.class);

    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Stream);
    when(mapper.readValue(s3Stream, EmailChallenge.class)).thenThrow(new RuntimeException("read boom"));

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.charger("abc"));

    assertTrue(ex.getMessage().contains("Erreur lors du chargement du challenge depuis S3"));
    assertEquals("read boom", ex.getCause().getMessage());
  }

  @Test
  void charger_s3ExceptionNon404WithAwsErrorDetails_throwsChallengeStorageException() {
    AwsErrorDetails details = AwsErrorDetails.builder().errorCode("AccessDenied").errorMessage("Access denied").build();

    S3Exception boom = (S3Exception) S3Exception.builder().statusCode(500).message("boom").awsErrorDetails(details).build();

    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(boom);

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.charger("abc"));

    assertTrue(ex.getMessage().contains("Erreur lors du chargement du challenge depuis S3"));
    assertSame(boom, ex.getCause());
  }

  @Test
  void sauvegarder_whenWriterFails_throwsChallengeStorageException() throws Exception {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.parse("2026-02-26T10:00:00Z")).expiresAt(Instant.parse("2026-02-26T10:10:00Z")).attempts(0).verified(false).build();

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(challenge)).thenThrow(new RuntimeException("write boom"));

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.sauvegarder("abc", challenge));

    assertTrue(ex.getMessage().contains("Erreur lors de la sauvegarde du challenge sur S3"));
    assertEquals("write boom", ex.getCause().getMessage());
  }

  @Test
  void sauvegarder_whenPutFailsWithAwsErrorDetails_throwsChallengeStorageException() throws Exception {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.parse("2026-02-26T10:00:00Z")).expiresAt(Instant.parse("2026-02-26T10:10:00Z")).attempts(0).verified(false).build();

    ObjectWriter writer = mock(ObjectWriter.class);
    when(mapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
    when(writer.writeValueAsBytes(challenge)).thenReturn("{}".getBytes(StandardCharsets.UTF_8));

    AwsErrorDetails details = AwsErrorDetails.builder().errorCode("AccessDenied").errorMessage("Access denied").build();

    S3Exception boom = (S3Exception) S3Exception.builder().statusCode(500).message("boom").awsErrorDetails(details).build();

    doThrow(boom).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    ChallengeStorageException ex = assertThrows(ChallengeStorageException.class, () -> adapter.sauvegarder("abc", challenge));

    assertTrue(ex.getMessage().contains("Erreur lors de la sauvegarde du challenge sur S3"));
    assertSame(boom, ex.getCause());
  }

  @Test
  void envoyerCode_whenSendFails_rethrowsException() {
    when(notifyService.resolveSubject(TemplateEmail.CHALLENGE, EmailLanguage.FR)).thenReturn("Votre code");
    when(notifyService.buildEmailBody("1234", TemplateEmail.CHALLENGE, EmailLanguage.FR)).thenReturn("BODY");
    doThrow(new RuntimeException("send boom")).when(notifyService).sendSimple("noreply@test.ch", "user@test.ch", "Votre code", "BODY");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> adapter.envoyerCode("user@test.ch", "1234", EmailLanguage.FR));

    assertEquals("send boom", ex.getMessage());
  }

  @Test
  void maskEmail_shouldReturnStarsWhenEmailIsNull() throws Exception {
    Method method = S3ChallengeEmailAdapter.class.getDeclaredMethod("maskEmail", String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(adapter, new Object[]{null});

    assertEquals("***", result);
  }

  @ParameterizedTest
  @CsvSource({"user@test.ch, us***@test.ch", "ab@test.ch, ***@test.ch", "invalid-email, ***"})
  void maskEmail_shouldReturnExpectedMaskedValue(String email, String expected) throws Exception {
    Method method = S3ChallengeEmailAdapter.class.getDeclaredMethod("maskEmail", String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(adapter, email);

    assertEquals(expected, result);
  }

  private static void setPrivateField(Object target, String fieldName, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field '" + fieldName + "' on " + target.getClass().getName(), e);
    }
  }
}
