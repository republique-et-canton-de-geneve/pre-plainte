package ch.ge.police.core.domain.model;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.core.domain.model.rendezvous.CreneauRendezVous;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;

class PrePlainteTest {

  private InformationsPersonnelles info;
  private Incident incident;
  private CreneauRendezVous creneau;

  @BeforeEach
  void setUp() {

    info = new InformationsPersonnelles();
    info.setNom("Dupont");
    info.setPrenom("Jean");
    info.setGenre(new RipolCode("1", "Masculin"));
    info.setNationalite(new RipolCode("8100", "Suisse"));
    info.setLieuOrigine(new RipolCode("6621", "Genève"));
    info.setDateNaissance("1990-01-01");
    info.setAdresse(new Adresse("Rue du Rhône 10", "Genève", "1204", "CH", null, "Suisse", null));
    info.setTelephone("41791234567");
    info.setEmail("jean.dupont@example.org");
    info.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    info.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    info.setNumeroDocumentIdentite("X1234567");
    info.setParlesFrancais(true);


    DommageMateriel dommage = new DommageMateriel();
    dommage.setDateDebutEvent("2025-10-21");
    dommage.setDateFinEvent("2025-10-21");
    dommage.setAdresseIncident(new Adresse("Rue du Marché 5", "Genève", "1204", "CH", null, "Suisse", null));
    dommage.setTypeDommage(TypeDommage.DOMMAGE_VEHICULE);
    dommage.setMontantEstime(500.0);
    dommage.setDevise("CHF");
    dommage.setNaturesDommage(List.of(NatureDommage.DEGRADATIONS));
    dommage.setDescription("Rayure sur le pare-chocs");
    dommage.setConstatPresent(false);

    incident = Incident.of(dommage);

    creneau = new CreneauRendezVous(
      "rdv-123",
      "2026-06-10",
      "09:00",
      "09:30",
      "Genève",
      "RDV01"
    );
  }

  @Test
  void shouldValidateSuccessfully() {
    PrePlainte prePlainte = new PrePlainte("id", info, incident);
    prePlainte.setCreneauRendezVous(creneau);
    assertDoesNotThrow(prePlainte::validateChampsPresPlainte);
  }

  @Test
  void shouldThrowWhenInformationsPersonnellesMissing() {
    PrePlainte prePlainte = new PrePlainte("id", null, incident);
    Exception ex = assertThrows(ValidationMetierException.class, prePlainte::validateChampsPresPlainte);
    assertTrue(ex.getMessage().contains("informations personnelles"));
  }

  @Test
  void shouldThrowWhenIncidentMissing() {
    PrePlainte prePlainte = new PrePlainte("id", info, null);
    Exception ex = assertThrows(ValidationMetierException.class, prePlainte::validateChampsPresPlainte);
    assertTrue(ex.getMessage().contains("incident"));
  }

  @Test
  void shouldThrowWhenOrganisationExpectedButMissing() {
    InformationsPersonnelles perso = info;
    perso.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    perso.setOrganisation(null);

    PrePlainte prePlainte = new PrePlainte("id",perso, incident);
    Exception ex = assertThrows(ValidationMetierException.class, prePlainte::validateChampsPresPlainte);
    assertTrue(ex.getMessage().contains("organisation"));
  }

  @Test
  void shouldThrowWhenTiersExpectedButMissing() {
    InformationsPersonnelles perso = info;
    perso.setLienAvecPersonne(LienAvecPersonne.TIERS);
    perso.setTiers(null);

    PrePlainte prePlainte = new PrePlainte("id", perso, incident);
    Exception ex = assertThrows(ValidationMetierException.class, prePlainte::validateChampsPresPlainte);
    assertTrue(ex.getMessage().contains("tiers"));
  }
}
