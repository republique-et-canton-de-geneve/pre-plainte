package ch.ge.police.core.domain.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailChallenge {
  private String  email;
  private String  codeHash;
  private Instant createdAt;
  private Instant lastCodeSentAt;
  private Instant expiresAt;
  private int     attempts;
  private boolean verified;
}
