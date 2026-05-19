package ch.ge.police.core.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.ge.police.core.application.service.EmailChallengeService.NotFoundException;
import ch.ge.police.core.application.service.EmailChallengeService.TooManyRequestsException;
import ch.ge.police.core.application.service.EmailChallengeService.VerifyResult;
import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.port.out.EmailChallengeStoragePort;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailChallengeServiceTest {

  @Mock
  EmailChallengeStoragePort storage;

  private EmailChallengeService service;

  @BeforeEach
  void setup() {
    service = new EmailChallengeService(storage);
    setPrivateField(service, "codeLength", 4);
    setPrivateField(service, "codeTtl", 2);
    setPrivateField(service, "maxAttempts", 3);
  }

  @Test
  void request_whenNoExisting_savesChallengeAndSendsCode() {
    when(storage.charger("key")).thenReturn(Optional.empty());

    ArgumentCaptor<EmailChallenge> challengeCaptor = ArgumentCaptor.forClass(EmailChallenge.class);
    ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

    service.request("user@test.ch", "key", EmailLanguage.FR);

    verify(storage).sauvegarder(eq("key"), challengeCaptor.capture());
    verify(storage).envoyerCode(eq("user@test.ch"), codeCaptor.capture(), eq(EmailLanguage.FR));
    verifyNoMoreInteractions(storage);

    EmailChallenge saved = challengeCaptor.getValue();
    assertEquals("user@test.ch", saved.getEmail());
    assertNotNull(saved.getCodeHash());
    assertFalse(saved.isVerified());
    assertEquals(0, saved.getAttempts());

    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getExpiresAt());
    assertTrue(saved.getExpiresAt().isAfter(saved.getCreatedAt()));

    String codeSent = codeCaptor.getValue();
    assertNotNull(codeSent);
    assertTrue(codeSent.chars().allMatch(Character::isDigit));
  }

  @Test
  void request_whenExistingNotVerifiedAndNotExpired_withinCooldown_throwsTooManyRequests() {
    Instant now = Instant.now();
    EmailChallenge existing = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(now.minus(1, ChronoUnit.HOURS)).lastCodeSentAt(now.minus(30, ChronoUnit.SECONDS)).expiresAt(now.plus(1, ChronoUnit.DAYS)).attempts(0).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(existing));

    TooManyRequestsException ex = assertThrows(TooManyRequestsException.class, () -> service.request("user@test.ch", "key", EmailLanguage.FR));
    assertTrue(ex.getMessage().contains("environ 2 minutes"));
    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void cooldownUserMessage_whenNoFullMinuteRemaining_mentionsSeconds() throws Exception {
    Method m = EmailChallengeService.class.getDeclaredMethod("cooldownUserMessage", Instant.class, Instant.class);
    m.setAccessible(true);
    Instant now = Instant.parse("2026-01-15T10:00:00Z");
    Instant nextAllowed = now;
    String msg = (String) m.invoke(null, nextAllowed, now);
    assertTrue(msg.contains("secondes"));
  }

  @Test
  void request_whenWithinCooldown_oneMinuteUserMessage() {
    Instant now = Instant.now();
    EmailChallenge existing = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(now.minus(1, ChronoUnit.HOURS)).lastCodeSentAt(now.minus(85, ChronoUnit.SECONDS)).expiresAt(now.plus(1, ChronoUnit.DAYS)).attempts(0).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(existing));

    TooManyRequestsException ex = assertThrows(TooManyRequestsException.class, () -> service.request("user@test.ch", "key", EmailLanguage.FR));
    assertTrue(ex.getMessage().contains("environ 1 minute"));
    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void request_whenExistingNotVerified_afterCooldown_regeneratesCodeAndSends() {
    Instant now = Instant.now();
    EmailChallenge existing = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(now.minus(1, ChronoUnit.HOURS)).lastCodeSentAt(now.minus(5, ChronoUnit.MINUTES)).expiresAt(now.plus(1, ChronoUnit.DAYS)).attempts(2).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(existing));

    service.request("user@test.ch", "key", EmailLanguage.FR);

    ArgumentCaptor<EmailChallenge> captor = ArgumentCaptor.forClass(EmailChallenge.class);
    verify(storage).sauvegarder(eq("key"), captor.capture());
    verify(storage).envoyerCode(eq("user@test.ch"), anyString(), eq(EmailLanguage.FR));

    EmailChallenge saved = captor.getValue();
    assertEquals(0, saved.getAttempts());
    assertNotNull(saved.getLastCodeSentAt());
    assertNotEquals("hash", saved.getCodeHash());
  }

  @Test
  void request_whenExistingExpired_allowsNewChallenge() {
    Instant now = Instant.now();
    EmailChallenge existing = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(now.minus(10, ChronoUnit.DAYS)).expiresAt(now.minus(1, ChronoUnit.DAYS)).attempts(0).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(existing));

    service.request("user@test.ch", "key", EmailLanguage.FR);

    verify(storage).sauvegarder(eq("key"), any(EmailChallenge.class));
    verify(storage).envoyerCode(eq("user@test.ch"), anyString(), eq(EmailLanguage.FR));
  }

  @Test
  void request_whenExistingVerified_allowsNewChallenge() {
    Instant now = Instant.now();
    EmailChallenge existing = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(now.minus(1, ChronoUnit.DAYS)).expiresAt(now.plus(1, ChronoUnit.DAYS)).attempts(0).verified(true).build();

    when(storage.charger("key")).thenReturn(Optional.of(existing));

    service.request("user@test.ch", "key", EmailLanguage.FR);

    verify(storage).sauvegarder(eq("key"), any(EmailChallenge.class));
    verify(storage).envoyerCode(eq("user@test.ch"), anyString(), eq(EmailLanguage.FR));
  }

  @Test
  void verify_whenNotFound_throwsNotFound() {
    when(storage.charger("key")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.verify("user@test.ch", "key", "1234"));
    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void verify_whenEmailMismatch_returnsInvalidAndDoesNotSave() {
    EmailChallenge challenge = EmailChallenge.builder().email("other@test.ch").codeHash("$2a$10$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").createdAt(Instant.now().minus(1, ChronoUnit.MINUTES)).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).attempts(0).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(challenge));
    VerifyResult res = service.verify("user@test.ch", "key", "1234");

    assertFalse(res.isSuccess());
    assertEquals("INVALID", res.getStatus());
    assertNull(res.getRemainingAttempts());

    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void verify_whenAlreadyVerified_returnsAlreadyVerified_andDoesNotSave() {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.now().minus(1, ChronoUnit.DAYS)).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).attempts(0).verified(true).build();

    when(storage.charger("key")).thenReturn(Optional.of(challenge));
    VerifyResult res = service.verify("user@test.ch", "key", "1234");

    assertTrue(res.isSuccess());
    assertEquals("ALREADY_VERIFIED", res.getStatus());
    assertNull(res.getRemainingAttempts());

    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void verify_whenExpired_returnsExpired_andDoesNotSave() {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.now().minus(3, ChronoUnit.DAYS)).expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)).attempts(0).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(challenge));
    VerifyResult res = service.verify("user@test.ch", "key", "1234");

    assertFalse(res.isSuccess());
    assertEquals("EXPIRED", res.getStatus());

    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void verify_whenLocked_returnsLocked_andDoesNotSave() {
    EmailChallenge challenge = EmailChallenge.builder().email("user@test.ch").codeHash("hash").createdAt(Instant.now().minus(1, ChronoUnit.MINUTES)).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).attempts(3).verified(false).build();

    when(storage.charger("key")).thenReturn(Optional.of(challenge));

    VerifyResult res = service.verify("user@test.ch", "key", "1234");

    assertFalse(res.isSuccess());
    assertEquals("LOCKED", res.getStatus());

    verify(storage).charger("key");
    verifyNoMoreInteractions(storage);
  }

  @Test
  void verify_whenCorrectCode_marksVerified_saves_andReturnsSuccess() {
    when(storage.charger("key")).thenReturn(Optional.empty());

    ArgumentCaptor<String> sentCodeCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<EmailChallenge> savedOnRequestCaptor = ArgumentCaptor.forClass(EmailChallenge.class);

    service.request("user@test.ch", "key", EmailLanguage.FR);

    verify(storage).sauvegarder(eq("key"), savedOnRequestCaptor.capture());
    verify(storage).envoyerCode(eq("user@test.ch"), sentCodeCaptor.capture(), eq(EmailLanguage.FR));

    EmailChallenge stored = savedOnRequestCaptor.getValue();
    String sentCode = sentCodeCaptor.getValue();

    reset(storage);
    when(storage.charger("key")).thenReturn(Optional.of(stored));
    ArgumentCaptor<EmailChallenge> savedOnVerifyCaptor = ArgumentCaptor.forClass(EmailChallenge.class);
    VerifyResult res = service.verify("user@test.ch", "key", sentCode);

    assertTrue(res.isSuccess());
    assertEquals("SUCCESS", res.getStatus());

    verify(storage).sauvegarder(eq("key"), savedOnVerifyCaptor.capture());

    EmailChallenge saved = savedOnVerifyCaptor.getValue();
    assertTrue(saved.isVerified());
    assertEquals(1, saved.getAttempts());
  }

  @Test
  void verify_whenWrongCode_incrementsAttempts_saves_andReturnsInvalidWithRemaining() {
    when(storage.charger("key")).thenReturn(Optional.empty());

    ArgumentCaptor<EmailChallenge> savedOnRequestCaptor = ArgumentCaptor.forClass(EmailChallenge.class);

    service.request("user@test.ch", "key", EmailLanguage.FR);
    verify(storage).sauvegarder(eq("key"), savedOnRequestCaptor.capture());

    EmailChallenge stored = savedOnRequestCaptor.getValue();
    reset(storage);
    when(storage.charger("key")).thenReturn(Optional.of(stored));
    ArgumentCaptor<EmailChallenge> savedOnVerifyCaptor = ArgumentCaptor.forClass(EmailChallenge.class);
    VerifyResult res = service.verify("user@test.ch", "key", "9999");

    assertFalse(res.isSuccess());
    assertEquals("INVALID", res.getStatus());
    assertEquals(Integer.valueOf(2), res.getRemainingAttempts());

    verify(storage).sauvegarder(eq("key"), savedOnVerifyCaptor.capture());
    EmailChallenge saved = savedOnVerifyCaptor.getValue();
    assertFalse(saved.isVerified());
    assertEquals(1, saved.getAttempts());
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
