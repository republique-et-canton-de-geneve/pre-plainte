package ch.ge.police.infrastructure.esirius.config;

import ch.ge.police.core.application.service.EsiriusService;
import ch.ge.police.core.port.in.EsiriusUseCase;
import ch.ge.police.core.port.out.EsiriusPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsiriusConfiguration {

  @Bean
  public EsiriusUseCase esiriusUseCase(EsiriusPort esiriusPort) {
    return new EsiriusService(esiriusPort);
  }
}
