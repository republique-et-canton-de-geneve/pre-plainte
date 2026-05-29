package ch.ge.police.core.application.service;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.demande.DemandeId;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.core.port.in.DemandeIdUseCase;
import ch.ge.police.core.port.in.PdfGenerationUseCase;
import ch.ge.police.core.port.out.PrePlainteBrouillontPort;
import ch.ge.police.core.port.out.PrePlainteSubmissionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrePlainteServiceTest {

  private PrePlainteBrouillontPort brouillonPort;
  private PrePlainteSubmissionPort submissionPort;
  private DemandeIdUseCase demandeIdUseCase;
  private PdfGenerationUseCase pdfGenerationUseCase;

  private PrePlainteService service;

  private PrePlainte prePlainte;

  @BeforeEach
  void setup() {
    brouillonPort = mock(PrePlainteBrouillontPort.class);
    submissionPort = mock(PrePlainteSubmissionPort.class);
    demandeIdUseCase = mock(DemandeIdUseCase.class);
    pdfGenerationUseCase = mock(PdfGenerationUseCase.class);

    service = new PrePlainteService(
      brouillonPort,
      submissionPort,
      demandeIdUseCase,
      pdfGenerationUseCase
    );

    prePlainte = buildValidPrePlainte();
  }

  private PrePlainte buildValidPrePlainte() {
    InformationsPersonnelles ip = new InformationsPersonnelles();
    ip.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    ip.setNom("TEST");
    ip.setPrenom("Test");
    ip.setGenre(new RipolCode("2", "Femme"));
    ip.setNationalite(new RipolCode("8100", "Suisse"));
    ip.setDateNaissance("1993-01-01");
    ip.setAdresse(new Adresse("Rue du Test 1", null, "1200", "CH", null, "Suisse", null));
    ip.setEmail("test@test.ch");
    ip.setTelephone("4178123456");
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    ip.setNumeroDocumentIdentite("X1234567");

    ObjetIncident objet = new ObjetIncident();
    objet.setType(new RipolCode("1", "Téléphone"));

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01");
    vol.setDateFinEvent("2025-01-01");
    vol.setAdresseIncident(new Adresse("Rue du Test 2", null, "1200", "CH", null, "Suisse", null));
    vol.setVolDansVehicule(true);
    vol.setObjetsVoles(List.of(objet));
    vol.setAvezVousDegradation(false);

    return new PrePlainte(null, ip, Incident.of(vol));
  }

  @Test
  void soumettrePrePlainteGeneratesIdAndSends() {
    String demandeId = "ID-123";
    when(demandeIdUseCase.generate(any())).thenReturn(new DemandeId(demandeId));

    Fichier pdf = new Fichier("file.pdf", "application/pdf", "base64");
    when(pdfGenerationUseCase.generateFilePdf(prePlainte)).thenReturn(pdf);

    String result = service.soumettrePrePlainte(prePlainte);

    assertEquals(demandeId, result);
    assertEquals(demandeId, prePlainte.getDemandeId());
    assertEquals(pdf, prePlainte.getPdfRecapitulatif());

    verify(pdfGenerationUseCase).generateFilePdf(prePlainte);
    verify(submissionPort).soumettrePrePlainteVersNas(prePlainte);
  }

  @Test
  void soumettrePrePlainteKeepsExistingId() {
    String demandeId = "EXISTING";
    prePlainte.setDemandeId(demandeId);

    Fichier pdf = new Fichier("file.pdf", "application/pdf", "base64");
    when(pdfGenerationUseCase.generateFilePdf(prePlainte)).thenReturn(pdf);

    String result = service.soumettrePrePlainte(prePlainte);

    assertEquals(demandeId, result);

    verify(demandeIdUseCase, never()).generate(any());
    verify(submissionPort).soumettrePrePlainteVersNas(prePlainte);
  }

  @Test
  void soumettrePrePlainteThrowsWhenPdfFails() {
    when(demandeIdUseCase.generate(any())).thenReturn(new DemandeId("ID-123"));
    when(pdfGenerationUseCase.generateFilePdf(prePlainte))
      .thenThrow(new RuntimeException("boom"));

    assertThrows(RuntimeException.class,
      () -> service.soumettrePrePlainte(prePlainte));

    verify(submissionPort, never()).soumettrePrePlainteVersNas(any());
  }

  @Test
  void recupererBrouillonReturnsValue() {
    when(brouillonPort.chargerBrouillon("ID-1")).thenReturn(prePlainte);

    PrePlainte result = service.recupererBrouillon("ID-1");

    assertEquals(prePlainte, result);
  }

  @Test
  void enregistrerPrePlainteEnModeBrouillonGeneratesId() {
    String demandeId = "ID-456";
    when(demandeIdUseCase.generate(any())).thenReturn(new DemandeId(demandeId));

    String result = service.enregistrerPrePlainteEnModeBrouillon(prePlainte);

    assertEquals(demandeId, result);
    assertEquals(demandeId, prePlainte.getDemandeId());

    verify(brouillonPort).sauvegarderBrouillon(prePlainte, demandeId);
  }

  @Test
  void enregistrerPrePlainteEnModeBrouillonKeepsExistingId() {
    String demandeId = "EXISTING";
    prePlainte.setDemandeId(demandeId);

    String result = service.enregistrerPrePlainteEnModeBrouillon(prePlainte);

    assertEquals(demandeId, result);

    verify(demandeIdUseCase, never()).generate(any());
    verify(brouillonPort).sauvegarderBrouillon(prePlainte, demandeId);
  }
}
