package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.InfosPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Communication;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NaturalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonType;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.LIEU_ORIGINE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.NATIONALITE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.SEXE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuisseEpolicePersonMapperTest {

  private SuisseEpoliceAddressMapper addressMapper;
  private SuisseEpolicePersonMapper mapper;

  @BeforeEach
  void setUp() {
    addressMapper = mock(SuisseEpoliceAddressMapper.class);
    mapper = new SuisseEpolicePersonMapper(addressMapper);
  }

  @Test
  void shouldReturnEmptyListWhenInfosIsNull() {
    List<Person> result = mapper.buildPersons(null, false, "aucune");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldBuildIndividualPersonWithoutVehicle() {
    InformationsPersonnelles infos = mockInformationsPersonnelles();
    when(infos.hasOrganisation()).thenReturn(false);
    when(infos.hasTiers()).thenReturn(false);

    List<Person> result = mapper.buildPersons(infos, false, "aucune");

    assertEquals(1, result.size());

    Person person = result.get(0);
    assertEquals(Ech051Constants.PERSON_KEY_TIERS, person.getKey());
    assertEquals(PersonType.NATURAL, person.getType());
    assertNotNull(person.getNaturalIdentity());
    assertNull(person.getLegalIdentity());
    assertNotNull(person.getCommunication());
  }

  @Test
  void shouldBuildIndividualPersonsWithVehicle() {
    InformationsPersonnelles infos = mockInformationsPersonnelles();
    when(infos.hasOrganisation()).thenReturn(false);
    when(infos.hasTiers()).thenReturn(false);

    List<Person> result = mapper.buildPersons(infos, true, "aucune");

    assertEquals(2, result.size());

    Person victim = result.get(0);
    Person insurance = result.get(1);

    assertEquals(Ech051Constants.PERSON_KEY_VEHICLE, victim.getKey());
    assertEquals(PersonType.NATURAL, victim.getType());

    assertEquals(Ech051Constants.INSURER_KEY_VEHICLE, insurance.getKey());
    assertEquals(PersonType.LEGAL, insurance.getType());
    assertEquals("aucune", insurance.getLegalIdentity().getCurrentName());
  }

  @Test
  void shouldBuildTiersPersons() {
    InformationsPersonnelles infos = mockInformationsPersonnelles();
    Tiers tiers = mock(Tiers.class);

    mockInfosPersonneCommon(tiers, "Dupont", "Jean");

    when(infos.hasOrganisation()).thenReturn(false);
    when(infos.hasTiers()).thenReturn(true);
    when(infos.getTiers()).thenReturn(tiers);

    List<Person> result = mapper.buildPersons(infos, false, "aucune");

    assertEquals(3, result.size());

    assertEquals(Ech051Constants.PERSON_KEY_TIERS, result.get(0).getKey());
    assertEquals(PersonType.NATURAL, result.get(0).getType());
    assertEquals("Dupont", result.get(0).getNaturalIdentity().getOfficialName());

    assertEquals(Ech051Constants.PERSON_KEY_DECLARANT, result.get(1).getKey());
    assertEquals(PersonType.NATURAL, result.get(1).getType());

    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_TIERS, result.get(2).getKey());
    assertEquals(PersonType.LEGAL, result.get(2).getType());
    assertEquals("aucune", result.get(2).getLegalIdentity().getCurrentName());
  }

  @Test
  void shouldBuildEntreprisePersons() {
    InformationsPersonnelles infos = mockInformationsPersonnelles();
    Organisation organisation = mock(Organisation.class);
    Adresse adresse = mock(Adresse.class);
    var mappedAddress = ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address.builder().street("Rue entreprise").build();

    when(infos.hasOrganisation()).thenReturn(true);
    when(infos.getOrganisation()).thenReturn(organisation);

    when(organisation.getNom()).thenReturn("ACME SA");
    when(organisation.getEmail()).thenReturn("contact@acme.ch");
    when(organisation.getTelephone()).thenReturn("0220000000");
    when(organisation.getAdresse()).thenReturn(adresse);

    when(addressMapper.fromAdresse(adresse)).thenReturn(mappedAddress);

    List<Person> result = mapper.buildPersons(infos, false, "Alliance");

    assertEquals(4, result.size());

    Person organisationPerson = result.get(0);
    Person declarant = result.get(1);
    Person informant = result.get(2);
    Person insurance = result.get(3);

    assertEquals(Ech051Constants.PERSON_KEY_ORGANISATION, organisationPerson.getKey());
    assertEquals(PersonType.LEGAL, organisationPerson.getType());
    assertEquals("ACME SA", organisationPerson.getLegalIdentity().getCurrentName());
    assertNotNull(organisationPerson.getCommunication());
    assertEquals("contact@acme.ch", organisationPerson.getCommunication().getEmail());
    assertEquals("0220000000", organisationPerson.getCommunication().getPhone());

    assertEquals(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE, declarant.getKey());
    assertEquals(PersonType.NATURAL, declarant.getType());

    assertEquals(Ech051Constants.PERSON_KEY_INFORMANT, informant.getKey());
    assertEquals(PersonType.NATURAL, informant.getType());
    assertEquals("Durand", informant.getNaturalIdentity().getOfficialName());
    assertEquals("Marie", informant.getNaturalIdentity().getFirstName());

    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, insurance.getKey());
    assertEquals(PersonType.LEGAL, insurance.getType());
    assertEquals("Alliance", insurance.getLegalIdentity().getCurrentName());
  }

  @Test
  void shouldBuildInsurancePerson() {
    Person result = mapper.buildInsurancePerson("INS1", null);

    assertEquals("INS1", result.getKey());
    assertEquals(PersonType.LEGAL, result.getType());
    assertNotNull(result.getLegalIdentity());
    assertEquals("aucune", result.getLegalIdentity().getCurrentName());

    Person named = mapper.buildInsurancePerson("INS2", "AXA");
    assertEquals("AXA", named.getLegalIdentity().getCurrentName());
  }

  @Test
  void shouldBuildNaturalPerson() {
    InfosPersonne infos = mockInfosPersonne("Durand", "Marie");
    Adresse adresse = mock(Adresse.class);
    var mappedAddress = ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address.builder().street("Rue test").build();

    when(infos.getAdresse()).thenReturn(adresse);
    when(addressMapper.fromAdresse(adresse)).thenReturn(mappedAddress);

    Person result = mapper.buildNaturalPerson("P1", "I1", infos, "info+");

    assertEquals("P1", result.getKey());
    assertEquals(PersonType.NATURAL, result.getType());
    assertEquals("info+", result.getAdditionalInformation());

    NaturalIdentity identity = result.getNaturalIdentity();
    assertNotNull(identity);
    assertEquals("I1", identity.getKey());
    assertEquals("Durand", identity.getOfficialName());
    assertEquals("NomNaissance", identity.getOriginalName());
    assertEquals("Marie", identity.getFirstName());
    assertEquals("1985-05-05", identity.getBirthDate());
    assertEquals(Ech051Constants.DEFAULT_LANGUAGE, identity.getLanguageCode());
    assertNotNull(identity.getSex());
    assertNotNull(identity.getPlaceOfOrigin());
    assertNotNull(identity.getNationality());

    assertNotNull(result.getCommunication());
    assertNotNull(result.getAddress());
  }

  @Test
  void shouldBuildPlaceOfOrigin() {
    InfosPersonne infos = mock(InfosPersonne.class);
    when(infos.getLieuOrigineCode()).thenReturn("1200");
    when(infos.getLieuOrigineLabel()).thenReturn("Genève");

    RipolLocation result = mapper.buildPlaceOfOrigin(infos);

    assertNotNull(result);
    assertEquals("1200", result.getCode());
    assertEquals("Genève", result.getLabel());
    assertEquals(LIEU_ORIGINE, result.getSourceTable());
    assertEquals("1200", result.getZipCode());
  }

  @Test
  void shouldReturnNullPlaceOfOriginWhenCodeMissing() {
    InfosPersonne infos = mock(InfosPersonne.class);

    assertNull(mapper.buildPlaceOfOrigin(infos));
  }

  @Test
  void shouldBuildSexReference() {
    InfosPersonne infos = mock(InfosPersonne.class);
    when(infos.getGenreCode()).thenReturn("M");
    when(infos.getGenreLabel()).thenReturn("Masculin");

    RipolReference result = mapper.buildSexReference(infos);

    assertNotNull(result);
    assertEquals("M", result.getCode());
    assertEquals("Masculin", result.getLabel());
    assertEquals(SEXE, result.getSourceTable());
    assertEquals("ISO", result.getSource());
  }

  @Test
  void shouldReturnNullSexReferenceWhenCodeMissing() {
    InfosPersonne infos = mock(InfosPersonne.class);

    assertNull(mapper.buildSexReference(infos));
  }

  @Test
  void shouldBuildNationalityReference() {
    InfosPersonne infos = mock(InfosPersonne.class);
    when(infos.getNationaliteCode()).thenReturn("CH");
    when(infos.getNationaliteLabel()).thenReturn("Suisse");

    RipolReference result = mapper.buildNationalityReference(infos);

    assertNotNull(result);
    assertEquals("CH", result.getCode());
    assertEquals("Suisse", result.getLabel());
    assertEquals(NATIONALITE, result.getSourceTable());
    assertEquals("RIPOL", result.getSource());
  }

  @Test
  void shouldReturnNullNationalityReferenceWhenCodeMissing() {
    InfosPersonne infos = mock(InfosPersonne.class);

    assertNull(mapper.buildNationalityReference(infos));
  }

  @Test
  void shouldBuildCommunication() {
    InfosPersonne infos = mock(InfosPersonne.class);
    when(infos.getEmail()).thenReturn("test@test.ch");
    when(infos.getTelephone()).thenReturn("0791234567");

    Communication result = mapper.buildCommunication(infos);

    assertNotNull(result);
    assertEquals("test@test.ch", result.getEmail());
    assertEquals("0791234567", result.getMobile());
  }

  @Test
  void shouldReturnNullCommunicationWhenNoEmailAndPhone() {
    InfosPersonne infos = mock(InfosPersonne.class);

    assertNull(mapper.buildCommunication(infos));
  }

  @Test
  void shouldResolveLanguageFromInformationsPersonnelles() {
    InformationsPersonnelles infos = mockInformationsPersonnelles();
    when(infos.getLangueCorrespondance()).thenReturn("de");

    String result = mapper.resolveLanguage(infos);

    assertEquals("de", result);
  }

  @Test
  void shouldResolveDefaultLanguageForOtherInfosPersonne() {
    InfosPersonne infos = mock(InfosPersonne.class);

    String result = mapper.resolveLanguage(infos);

    assertEquals(Ech051Constants.DEFAULT_LANGUAGE, result);
  }

  private InformationsPersonnelles mockInformationsPersonnelles() {
    InformationsPersonnelles infos = mock(InformationsPersonnelles.class);
    Adresse adresse = mock(Adresse.class);
    var mappedAddress = ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address.builder().street("Rue test").build();

    when(infos.getNom()).thenReturn("Durand");
    when(infos.getNomNaissance()).thenReturn("NomNaissance");
    when(infos.getPrenom()).thenReturn("Marie");
    when(infos.getDateNaissance()).thenReturn("1990-01-01");
    when(infos.getGenreCode()).thenReturn("F");
    when(infos.getGenreLabel()).thenReturn("Féminin");
    when(infos.getLieuOrigineCode()).thenReturn("1200");
    when(infos.getLieuOrigineLabel()).thenReturn("Genève");
    when(infos.getNationaliteCode()).thenReturn("CH");
    when(infos.getNationaliteLabel()).thenReturn("Suisse");
    when(infos.getEmail()).thenReturn("marie@test.ch");
    when(infos.getTelephone()).thenReturn("0790000000");
    when(infos.getAdresse()).thenReturn(adresse);

    when(addressMapper.fromAdresse(adresse)).thenReturn(mappedAddress);

    return infos;
  }

  private InfosPersonne mockInfosPersonne(String nom, String prenom) {
    InfosPersonne infos = mock(InfosPersonne.class);
    Adresse adresse = mock(Adresse.class);
    var mappedAddress = ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address.builder().street("Rue tiers").build();

    when(infos.getNom()).thenReturn(nom);
    when(infos.getNomNaissance()).thenReturn("NomNaissance");
    when(infos.getPrenom()).thenReturn(prenom);
    when(infos.getDateNaissance()).thenReturn("1985-05-05");
    when(infos.getGenreCode()).thenReturn("M");
    when(infos.getGenreLabel()).thenReturn("Masculin");
    when(infos.getLieuOrigineCode()).thenReturn("1000");
    when(infos.getLieuOrigineLabel()).thenReturn("Lausanne");
    when(infos.getNationaliteCode()).thenReturn("CH");
    when(infos.getNationaliteLabel()).thenReturn("Suisse");
    when(infos.getEmail()).thenReturn("tiers@test.ch");
    when(infos.getTelephone()).thenReturn("0781111111");
    when(infos.getAdresse()).thenReturn(adresse);

    when(addressMapper.fromAdresse(adresse)).thenReturn(mappedAddress);

    return infos;
  }

  private void mockInfosPersonneCommon(InfosPersonne infos, String nom, String prenom) {
    Adresse adresse = mock(Adresse.class);
    var mappedAddress = ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address.builder().street("Rue tiers").build();

    when(infos.getNom()).thenReturn(nom);
    when(infos.getNomNaissance()).thenReturn("NomNaissance");
    when(infos.getPrenom()).thenReturn(prenom);
    when(infos.getDateNaissance()).thenReturn("1985-05-05");
    when(infos.getGenreCode()).thenReturn("M");
    when(infos.getGenreLabel()).thenReturn("Masculin");
    when(infos.getLieuOrigineCode()).thenReturn("1000");
    when(infos.getLieuOrigineLabel()).thenReturn("Lausanne");
    when(infos.getNationaliteCode()).thenReturn("CH");
    when(infos.getNationaliteLabel()).thenReturn("Suisse");
    when(infos.getEmail()).thenReturn("tiers@test.ch");
    when(infos.getTelephone()).thenReturn("0781111111");
    when(infos.getAdresse()).thenReturn(adresse);

    when(addressMapper.fromAdresse(adresse)).thenReturn(mappedAddress);
  }
}
