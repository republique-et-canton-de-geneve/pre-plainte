package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.port.out.EmailChallengeStoragePort;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailChallengeService {
  private final EmailChallengeStoragePort storage;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private final SecureRandom random = new SecureRandom();
  private static final int NUMERIC_BASE = 10;

  @Value("${mail.challenge.codeLength}")
  private int codeLength;
  @Value("${mail.challenge.codeTtlDays}")
  private int codeTtl;
  @Value("${mail.challenge.maxAttempts}")
  private int maxAttempts;

  private static final int RESEND_COOLDOWN_MINUTES = 2;
  /** Arrondi supérieur des secondes restantes vers la minute affichée (ex. 61 s → 2 min). */
  private static final long COOLDOWN_SECONDS_ROUND_UP_OFFSET = 59L;
  private static final long SECONDS_PER_MINUTE = 60L;

  public void request(String email, String key, EmailLanguage language) {
    Instant now = Instant.now();

    var existing = storage.charger(key).orElse(null);
    if (existing != null && !existing.isVerified() && now.isBefore(existing.getExpiresAt())) {
      Instant lastSent =
          existing.getLastCodeSentAt() != null ? existing.getLastCodeSentAt() : existing.getCreatedAt();
      Instant nextAllowed = lastSent.plus(RESEND_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
      if (now.isBefore(nextAllowed)) {
        throw new TooManyRequestsException(cooldownUserMessage(nextAllowed, now));
      }
      String code = generateNumericCode(codeLength);
      String hash = encoder.encode(code);
      existing.setEmail(email);
      existing.setCodeHash(hash);
      existing.setAttempts(0);
      existing.setLastCodeSentAt(now);
      storage.sauvegarder(key, existing);
      storage.envoyerCode(email, code, language);
      return;
    }

    String code = generateNumericCode(codeLength);
    String hash = encoder.encode(code);

    EmailChallenge challenge =
        EmailChallenge.builder()
            .email(email)
            .codeHash(hash)
            .createdAt(now)
            .lastCodeSentAt(now)
            .expiresAt(now.plus(codeTtl, ChronoUnit.DAYS))
            .attempts(0)
            .verified(false)
            .build();

    storage.sauvegarder(key, challenge);
    storage.envoyerCode(email, code, language);
  }

  private static String cooldownUserMessage(Instant nextAllowed, Instant now) {
    long seconds = Math.max(0, nextAllowed.getEpochSecond() - now.getEpochSecond());
    long minutes = (seconds + COOLDOWN_SECONDS_ROUND_UP_OFFSET) / SECONDS_PER_MINUTE;
    if (minutes <= 0) {
      return "Veuillez patienter quelques secondes avant de pouvoir renvoyer un code.";
    }
    if (minutes == 1) {
      return "Un code vient d'être envoyé. Vous pourrez en demander un nouveau dans environ 1 minute. "
          + "En attendant, saisissez le code reçu par e-mail (vérifiez vos courriers indésirables).";
    }
    return "Un code vient d'être envoyé. Vous pourrez en demander un nouveau dans environ "
        + minutes
        + " minutes. En attendant, saisissez le code reçu par e-mail (vérifiez vos courriers indésirables).";
  }

  public VerifyResult verify(String email, String key, String code) {
    Instant now = Instant.now();

    EmailChallenge challenge = storage.charger(key).orElseThrow(() -> new NotFoundException("Aucun challenge trouvé."));

    if (!challenge.getEmail().equalsIgnoreCase(email)) {
      return VerifyResult.invalid(null);
    }

    if (challenge.isVerified()) {
      return VerifyResult.alreadyVerified();
    }
    if (now.isAfter(challenge.getExpiresAt())) {
      return VerifyResult.expired();
    }
    if (challenge.getAttempts() >= maxAttempts) {
      return VerifyResult.locked();
    }

    boolean ok = encoder.matches(code, challenge.getCodeHash());

    challenge.setAttempts(challenge.getAttempts() + 1);

    if (ok) {
      challenge.setVerified(true);
      storage.sauvegarder(key, challenge);
      return VerifyResult.success();
    }

    storage.sauvegarder(key, challenge);
    int remaining = Math.max(0, maxAttempts - challenge.getAttempts());
    return VerifyResult.invalid(remaining);
  }

  private String generateNumericCode(int length) {
    if (length <= 0 || length > NUMERIC_BASE - 1) {
      throw new IllegalArgumentException("length must be between 1 and 9");
    }
    int floor = 1;
    for (int i = 1; i < length; i++) {
      floor *= NUMERIC_BASE;
    }
    int bound = floor * (NUMERIC_BASE - 1);
    return String.valueOf(floor + random.nextInt(bound));
  }

  @Getter
  public static class VerifyResult {
    private final boolean success;
    private final String status;
    private final Integer remainingAttempts;

    public VerifyResult(boolean success, String status, Integer remainingAttempts) {
      this.success = success;
      this.status = status;
      this.remainingAttempts = remainingAttempts;
    }

    public static VerifyResult success() {
      return new VerifyResult(true, "SUCCESS", null);
    }

    public static VerifyResult invalid(Integer remaining) {
      return new VerifyResult(false, "INVALID", remaining);
    }

    public static VerifyResult expired() {
      return new VerifyResult(false, "EXPIRED", null);
    }

    public static VerifyResult locked() {
      return new VerifyResult(false, "LOCKED", null);
    }

    public static VerifyResult alreadyVerified() {
      return new VerifyResult(true, "ALREADY_VERIFIED", null);
    }
  }

  public static class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
      super(message);
    }
  }

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
      super(message);
    }
  }
}
