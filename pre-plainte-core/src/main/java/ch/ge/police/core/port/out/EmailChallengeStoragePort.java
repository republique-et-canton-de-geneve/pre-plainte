package ch.ge.police.core.port.out;

import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.notification.EmailLanguage;

import java.util.Optional;

public interface EmailChallengeStoragePort {
  Optional<EmailChallenge> charger(String key);
  void sauvegarder(String key, EmailChallenge challenge);
  void envoyerCode(String email, String code, EmailLanguage language);
}
