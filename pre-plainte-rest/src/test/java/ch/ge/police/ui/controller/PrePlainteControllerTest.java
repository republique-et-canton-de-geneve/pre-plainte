package ch.ge.police.ui.controller;

import ch.ge.police.core.application.service.NotifyService;
import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import ch.ge.police.core.domain.model.notification.TemplateEmail;
import ch.ge.police.core.domain.util.FichierValidator;
import ch.ge.police.core.port.in.PdfGenerationUseCase;
import ch.ge.police.core.port.in.PrePlainteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PrePlainteControllerTest {

  private PrePlainteUseCase prePlainteUseCase;
  private NotifyService notifyService;
  private PdfGenerationUseCase pdfGenerationUseCase;
  private PrePlainteController controller;

  @BeforeEach
  void setUp() {
    prePlainteUseCase = mock(PrePlainteUseCase.class);
    notifyService = mock(NotifyService.class);
    pdfGenerationUseCase = mock(PdfGenerationUseCase.class);
    controller = new PrePlainteController(prePlainteUseCase, notifyService, pdfGenerationUseCase);
    ReflectionTestUtils.setField(controller, "from", "noreply@test.ch");
    ReflectionTestUtils.setField(controller, "url", "https://front.test/preplainte");
  }

  @Test
  void enregistrerBrouillon_validatesFiles_savesDraft_buildsMailAndSendsIt() {
    PrePlainte prePlainte = mock(PrePlainte.class, RETURNS_DEEP_STUBS);

    when(prePlainte.getInformationsPersonnelles().getEmail()).thenReturn("user@test.ch");
    when(prePlainte.getIncident()).thenReturn(null);

    when(prePlainteUseCase.enregistrerPrePlainteEnModeBrouillon(prePlainte)).thenReturn("draft-123");
    when(notifyService.resolveSubject(TemplateEmail.BROUILLON, EmailLanguage.FR)).thenReturn("Enregistrement de votre brouillon");
    when(notifyService.buildEmailBody("https://front.test/preplainte?demandeId=draft-123", TemplateEmail.BROUILLON, EmailLanguage.FR)).thenReturn("BODY");

    try (MockedStatic<FichierValidator> mockedValidator = mockStatic(FichierValidator.class)) {
      controller.enregistrerBrouillon(prePlainte, Locale.FRENCH);

      verify(prePlainteUseCase).enregistrerPrePlainteEnModeBrouillon(prePlainte);
      verify(notifyService).resolveSubject(TemplateEmail.BROUILLON, EmailLanguage.FR);
      verify(notifyService).buildEmailBody("https://front.test/preplainte?demandeId=draft-123", TemplateEmail.BROUILLON, EmailLanguage.FR);
      verify(notifyService).sendSimple("noreply@test.ch", "user@test.ch", "Enregistrement de votre brouillon", "BODY");
      verifyNoMoreInteractions(notifyService);
    }
  }

  @Test
  void recupererBrouillon_returnsUseCaseResult() {
    PrePlainte expected = mock(PrePlainte.class);
    when(prePlainteUseCase.recupererBrouillon("draft-123")).thenReturn(expected);

    PrePlainte result = controller.recupererBrouillon("draft-123");

    assertSame(expected, result);
    verify(prePlainteUseCase).recupererBrouillon("draft-123");
  }

  @Test
  void soumettrePrePlainte_validatesAllFileCollections_andDelegatesToUseCase() {
    PrePlainte prePlainte = mock(PrePlainte.class, RETURNS_DEEP_STUBS);
    Cybercrime cybercrime = mock(Cybercrime.class);
    List<?> fichiersCybercrime = mock(List.class);

    when(prePlainte.getIncident().getDetails()).thenReturn(cybercrime);
    when(cybercrime.getFichiersCybercrime()).thenReturn((List) fichiersCybercrime);
    when(prePlainteUseCase.soumettrePrePlainte(prePlainte)).thenReturn("demande-456");

    try (MockedStatic<FichierValidator> mockedValidator = mockStatic(FichierValidator.class)) {
      String result = controller.soumettrePrePlainte(prePlainte);

      assertEquals("demande-456", result);
      mockedValidator.verify(() -> FichierValidator.validateFichiers((List) fichiersCybercrime), times(1));
      verify(prePlainteUseCase).soumettrePrePlainte(prePlainte);
    }
  }

  @Test
  void soumettrePrePlainte_whenNoIncident_doesNotValidateFiles() {
    PrePlainte prePlainte = mock(PrePlainte.class, RETURNS_DEEP_STUBS);

    when(prePlainte.getIncident()).thenReturn(null);
    when(prePlainteUseCase.soumettrePrePlainte(prePlainte)).thenReturn("demande-789");

    try (MockedStatic<FichierValidator> mockedValidator = mockStatic(FichierValidator.class)) {
      String result = controller.soumettrePrePlainte(prePlainte);

      assertEquals("demande-789", result);
      mockedValidator.verifyNoMoreInteractions();
      verify(prePlainteUseCase).soumettrePrePlainte(prePlainte);
    }
  }

  @Test
  void generatePdf_returnsPdfResponseWithAttachmentHeader() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    byte[] pdf = new byte[]{1, 2, 3};

    when(prePlainte.getDemandeId()).thenReturn("demande-999");
    when(pdfGenerationUseCase.generatePdf(prePlainte)).thenReturn(pdf);

    ResponseEntity<byte[]> response = controller.generatePdf(prePlainte);

    assertEquals(200, response.getStatusCode().value());
    assertArrayEquals(pdf, response.getBody());
    assertEquals("attachment; filename=\"demande-999_" + LocalDate.now() + ".pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    verify(pdfGenerationUseCase).generatePdf(prePlainte);
  }
}
