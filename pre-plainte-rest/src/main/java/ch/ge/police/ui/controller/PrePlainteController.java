package ch.ge.police.ui.controller;

import ch.ge.police.core.application.service.NotifyService;
import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.domain.model.notification.TemplateEmail;
import ch.ge.police.core.domain.util.FichierValidator;
import ch.ge.police.core.port.in.PdfGenerationUseCase;
import ch.ge.police.core.port.in.PrePlainteUseCase;

import java.time.LocalDate;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preplainte")
@RequiredArgsConstructor
@Slf4j
public class PrePlainteController {
  private final PrePlainteUseCase prePlainteUseCase;
  private final NotifyService notifyService;
  private final PdfGenerationUseCase pdfGenerator;
  @Value("${spring.mail.from}")
  private String from;
  @Value("${frontend.url}")
  private String url;

  @PostMapping("/draft")
  public void enregistrerBrouillon(@RequestBody PrePlainte prePlainte, Locale locale) {
    validateAllFichiers(prePlainte);
    String demandeId = prePlainteUseCase.enregistrerPrePlainteEnModeBrouillon(prePlainte);
    String link = url + "?demandeId=" + demandeId;
    EmailLanguage language = EmailLanguage.fromLocale(locale);
    String subject = notifyService.resolveSubject(TemplateEmail.BROUILLON, language);
    String body = notifyService.buildEmailBody(link, TemplateEmail.BROUILLON, language);
    notifyService.sendSimple(from, prePlainte.getInformationsPersonnelles().getEmail(), subject, body);
  }

  @GetMapping("/draft/{demandeId}")
  public PrePlainte recupererBrouillon(@PathVariable String demandeId) {
    return prePlainteUseCase.recupererBrouillon(demandeId);
  }

  @PostMapping("/soumission")
  public String soumettrePrePlainte(@RequestBody PrePlainte prePlainte) {
    validateAllFichiers(prePlainte);
    return prePlainteUseCase.soumettrePrePlainte(prePlainte);
  }

  private void validateAllFichiers(PrePlainte prePlainte) {
    if (prePlainte.getIncident() != null ) {
      FichierValidator.validateFichiers(prePlainte.getIncident().getDetails().getFichiers());
      if (prePlainte.getIncident().getDetails() instanceof Cybercrime cyber) {
        FichierValidator.validateFichiers(cyber.getFichiersCybercrime());
      }
    }
  }

  @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> generatePdf(@RequestBody PrePlainte prePlainte) {

    byte[] pdf = pdfGenerator.generatePdf(prePlainte);

    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prePlainte.getDemandeId() + "_" + LocalDate.now() + ".pdf\"").body(pdf);
  }
}
