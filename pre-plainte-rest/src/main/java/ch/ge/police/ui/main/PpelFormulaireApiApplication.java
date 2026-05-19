package ch.ge.police.ui.main;

import ch.ge.police.core.application.service.PrePlainteService;
import ch.ge.police.core.port.in.DemandeIdUseCase;
import ch.ge.police.core.port.in.PrePlainteUseCase;
import ch.ge.police.core.port.out.PrePlainteBrouillontPort;
import ch.ge.police.core.port.out.PrePlainteSubmissionPort;
import ch.ge.police.infrastructure.pdf.PdfGenerationAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "ch.ge.police")
public class PpelFormulaireApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(PpelFormulaireApiApplication.class, args);
  }

  @Bean
  public PrePlainteUseCase prePlainteUseCase(
    PrePlainteBrouillontPort prePlainteBrouillonPort,
    PrePlainteSubmissionPort prePlainteSubmissionPort,
    DemandeIdUseCase demandeIdUseCase,
    PdfGenerationAdapter pdfGenerationAdapter
  ) {
    return new PrePlainteService(prePlainteBrouillonPort, prePlainteSubmissionPort, demandeIdUseCase, pdfGenerationAdapter);
  }
}
