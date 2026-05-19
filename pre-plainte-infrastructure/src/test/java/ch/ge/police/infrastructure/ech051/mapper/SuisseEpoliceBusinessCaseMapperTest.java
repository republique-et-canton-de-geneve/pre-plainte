package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuisseEpoliceBusinessCaseMapperTest {

  private SuisseEpoliceBusinessCaseMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new SuisseEpoliceBusinessCaseMapper();
  }

  private InformationsPersonnelles baseInfos() {
    InformationsPersonnelles ip = new InformationsPersonnelles();
    ip.setNom("N");
    ip.setPrenom("P");
    ip.setGenre(new RipolCode("1", "M"));
    ip.setNationalite(new RipolCode("8100", "CH"));
    ip.setDateNaissance("1990-01-01");
    ip.setAdresse(new Adresse("Rue", "1", "1200", "Genève", "1200", "CH", "8100"));
    ip.setTelephone("41790000000");
    ip.setEmail("a@b.ch");
    ip.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    ip.setNumeroDocumentIdentite("AB");
    return ip;
  }

  @Test
  void buildBusinessCase_individualUsesDefaultKey() {
    PrePlainte p = new PrePlainte("D-1", baseInfos(), Incident.of(minimalVol()));
    BusinessCase bc = mapper.buildBusinessCase(p, DeclarationType.INDIVIDUAL);
    assertEquals(Ech051Constants.BUSINESS_CASE_KEY, bc.getKey());
    assertEquals("D-1", bc.getCaseNumber());
    assertNotNull(bc.getFile());
  }

  @Test
  void buildBusinessCase_tiersUsesTiersKey() {
    PrePlainte p = new PrePlainte("D-2", baseInfos(), Incident.of(minimalVol()));
    BusinessCase bc = mapper.buildBusinessCase(p, DeclarationType.TIERS);
    assertEquals(Ech051Constants.BUSINESS_CASE_KEY_TIERS, bc.getKey());
  }

  @Test
  void buildBusinessCase_entrepriseUsesEntrepriseKey() {
    PrePlainte p = new PrePlainte("D-3", baseInfos(), Incident.of(minimalVol()));
    BusinessCase bc = mapper.buildBusinessCase(p, DeclarationType.ENTREPRISE);
    assertEquals(Ech051Constants.BUSINESS_CASE_KEY_ENTREPRISE, bc.getKey());
  }

  @Test
  void buildBusinessCase_nullDemandeIdFallsBackToPpl() {
    PrePlainte p = new PrePlainte(null, baseInfos(), Incident.of(minimalVol()));
    BusinessCase bc = mapper.buildBusinessCase(p, DeclarationType.INDIVIDUAL);
    assertEquals("PPL", bc.getCaseNumber());
  }

  @Test
  void buildBusinessCase_collectsAttachmentsFromIncidentAndCybercrime() {
    InformationsPersonnelles ip = baseInfos();

    ip.setLienAvecPersonne(LienAvecPersonne.TIERS);

    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("V");
    achat.setPrenomVendeur("W");
    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setFichiersCybercrime(List.of(new Fichier("cyber.pdf", "application/pdf", "QkM=")));

    PrePlainte p = new PrePlainte("D-CYB", ip, Incident.of(cyber));
    BusinessCase bc = mapper.buildBusinessCase(p, DeclarationType.TIERS);

    assertEquals(1, bc.getFile().getFirst().getAttachment().size());
    assertTrue(bc.getFile().getFirst().getAttachment().stream().anyMatch(a -> "cyber.pdf".equals(a.getFilename())));
  }

  private static Vol minimalVol() {
    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01");
    vol.setDateFinEvent("2025-01-01");
    return vol;
  }
}
