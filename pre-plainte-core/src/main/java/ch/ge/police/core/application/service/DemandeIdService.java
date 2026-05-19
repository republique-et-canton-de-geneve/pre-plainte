package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.demande.DemandeId;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.util.IdGenerator;
import ch.ge.police.core.port.in.DemandeIdUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemandeIdService implements DemandeIdUseCase {

  private static final String PREFIX = "AEL-PPL-";
  private static final int RANDOM_PART_LENGTH = 10;

  @Override
  public DemandeId generate(TypeIncident typeIncident) {
    String randomPart = IdGenerator.randomAlphanumeric(RANDOM_PART_LENGTH).toUpperCase(Locale.ROOT);
    String fullId = PREFIX + (typeIncident != null ? (typeIncident.code() + "-" + randomPart) : randomPart);
    return new DemandeId(fullId);
  }
}
