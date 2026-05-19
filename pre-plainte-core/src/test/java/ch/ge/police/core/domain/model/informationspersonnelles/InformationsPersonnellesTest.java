package ch.ge.police.core.domain.model.informationspersonnelles;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;

class InformationsPersonnellesTest {

  private InformationsPersonnelles createValidBase() {
    InformationsPersonnelles info = new InformationsPersonnelles();
    info.setNom("Toure");
    info.setPrenom("Maka");
    info.setGenre(new RipolCode("1", "Homme"));
    info.setNationalite(new RipolCode("8100", "Suisse"));
    info.setDateNaissance("1993-11-27");
    info.setAdresse(new Adresse("Route des Fayards", "Versoix", "1290", "CH", "1234", "Suisse", "8212"));
    info.setTelephone("41789000000");
    info.setEmail("test@example.com");
    info.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    info.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    info.setNumeroDocumentIdentite("AB123456");
    info.setParlesFrancais(true);
    return info;
  }

  @Test
  void shouldValidateSuccessfullyForSelfDeclaration() {
    InformationsPersonnelles info = createValidBase();
    assertDoesNotThrow(info::validate);
  }


  @Test
  void shouldThrowWhenNoFrenchAndNoLanguageProvided() {
    InformationsPersonnelles info = createValidBase();
    info.setParlesFrancais(false);
    info.setLangueCorrespondance(null);

    Exception ex = assertThrows(ValidationMetierException.class, info::validate);
    assertTrue(ex.getMessage().contains("langue de correspondance"));
  }

  @Test
  void shouldValidateSuccessfullyWithTiers() {
    InformationsPersonnelles info = createValidBase();
    info.setLienAvecPersonne(LienAvecPersonne.TIERS);
    info.setTypeRepresentation("Famille");

    Tiers tiers = new Tiers();
    tiers.setNom("Dupont");
    tiers.setPrenom("Jean");
    tiers.setGenre(new RipolCode("1", "Homme"));
    tiers.setNationalite(new RipolCode("8200", "France"));
    tiers.setDateNaissance("1980-05-05");
    tiers.setAdresse(new Adresse("Rue du Lac", "Genève", "1200", "CH", "1234", "Suisse", "8212"));
    tiers.setTelephone("41780000000");
    tiers.setEmail("jean.dupont@example.com");
    tiers.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    tiers.setNumeroDocumentIdentite("CD987654");

    info.setTiers(tiers);

    assertDoesNotThrow(info::validate);
  }

  @Test
  void shouldThrowIfTiersIsMissingWhenLienIsTiers() {
    InformationsPersonnelles info = createValidBase();
    info.setLienAvecPersonne(LienAvecPersonne.TIERS);
    info.setTiers(null);

    Exception ex = assertThrows(ValidationMetierException.class, info::validate);
    assertTrue(ex.getMessage().contains("tiers"));
  }

  @Test
  void shouldValidateSuccessfullyWithOrganisation() {
    InformationsPersonnelles info = createValidBase();
    info.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    info.setPostePersonneMorale("Directeur");

    Organisation org = new Organisation();
    org.setNom("Keylity SA");
    org.setAdresse(new Adresse("Rue du Marché 10", "Genève", "1204", "CH", "1234", "Suisse", "8212"));
    org.setTelephone("41220000000");
    org.setEmail("contact@keylity.ch");

    info.setOrganisation(org);

    assertDoesNotThrow(info::validate);
  }

  @Test
  void shouldThrowIfOrganisationMissingWhenLienIsEntreprise() {
    InformationsPersonnelles info = createValidBase();
    info.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    info.setOrganisation(null);

    Exception ex = assertThrows(ValidationMetierException.class, info::validate);
    assertTrue(ex.getMessage().contains("organisation"));
  }
}
