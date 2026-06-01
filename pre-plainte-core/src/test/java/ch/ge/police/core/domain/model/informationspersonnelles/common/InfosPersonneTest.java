package ch.ge.police.core.domain.model.informationspersonnelles.common;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.common.error.ValidationMetierException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfosPersonneTest {

  private InfosPersonne createValidPerson() {
    InfosPersonne p = new InfosPersonne();
    p.setNom("Toure");
    p.setPrenom("Maka");
    p.setGenre(new RipolCode("1", "Homme"));
    p.setNationalite(new RipolCode("8100", "Suisse"));
    p.setDateNaissance("1993-11-27");
    p.setAdresse(new Adresse("Route des Fayards", "Versoix", "1290", "CH", null, "Suisse", null));
    p.setTelephone("41789000000");
    p.setEmail("maka@example.com");
    p.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    p.setNumeroDocumentIdentite("AB123456");
    return p;
  }

  @Test
  void shouldValidateSuccessfully() {
    InfosPersonne p = createValidPerson();
    assertDoesNotThrow(p::validateBasicInfo);
  }

  @Test
  void shouldThrowWhenNomMissing() {
    InfosPersonne p = createValidPerson();
    p.setNom(null);
    Exception ex = assertThrows(ValidationMetierException.class, p::validateBasicInfo);
    assertTrue(ex.getMessage().contains("nom"));
  }

  @Test
  void shouldThrowWhenPrenomMissing() {
    InfosPersonne p = createValidPerson();
    p.setPrenom("");
    Exception ex = assertThrows(ValidationMetierException.class, p::validateBasicInfo);
    assertTrue(ex.getMessage().contains("prénom"));
  }

  @Test
  void shouldThrowWhenAdresseMissing() {
    InfosPersonne p = createValidPerson();
    p.setAdresse(null);
    Exception ex = assertThrows(ValidationMetierException.class, p::validateBasicInfo);
    assertTrue(ex.getMessage().contains("adresse"));
  }

  @Test
  void shouldNotThrowWhenNumeroDocumentIdentiteMissingForDocumentsVolesPerdus() {
    InfosPersonne p = createValidPerson();
    p.setTypeDocumentIdentite(TypeDocumentIdentite.DOCUMENTS_VOLES_PERDUS);
    p.setNumeroDocumentIdentite(null);
    assertDoesNotThrow(p::validateBasicInfo);
  }
}
