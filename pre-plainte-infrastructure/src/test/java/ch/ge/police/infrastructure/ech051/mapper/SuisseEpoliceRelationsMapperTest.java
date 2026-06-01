package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.MoyenPaiement;
import ch.ge.police.core.domain.model.event.cybercrime.common.PlateformeUtilisee;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.LegalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NaturalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonType;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Relations;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuisseEpoliceRelationsMapperTest {

  private SuisseEpoliceRelationsMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new SuisseEpoliceRelationsMapper();
  }

  @Test
  void shouldBuildIndividualRelations() {
    List<Person> persons = List.of(person("P1"));
    List<Event> events = List.of(event("E1"));
    List<ObjectItem> objects = List.of(object("O1"));
    List<VehicleItem> vehicles = List.of(vehicle("V1"));
    BusinessCase businessCase = businessCase("B1");

    Relations result = mapper.buildRelations(persons, events, objects, vehicles, businessCase, DeclarationType.INDIVIDUAL, null);

    assertNotNull(result);
    assertEquals(1, size(result.getInvolvedParties()));
    assertEquals(1, size(result.getEventBusinessCaseLinks()));
    assertEquals(1, size(result.getEventObjectLinks()));
    assertEquals(1, size(result.getObjectPersonLinks()));
    assertEquals(1, size(result.getEventVehicleLinks()));
    assertEquals(1, size(result.getVehiclePersonLinks()));

    assertEquals("P1", result.getInvolvedParties().getFirst().getPersonRef());
    assertEquals("E1", result.getInvolvedParties().getFirst().getEventRef());
    assertEquals("B1", result.getInvolvedParties().getFirst().getBusinessCaseRef());

    assertEquals("O1", result.getObjectPersonLinks().getFirst().getObjectRef());
    assertEquals("P1", result.getObjectPersonLinks().getFirst().getPersonRef());

    assertEquals("V1", result.getVehiclePersonLinks().getFirst().getVehicleRef());
    assertEquals("P1", result.getVehiclePersonLinks().getFirst().getPersonRef());
    assertEquals(Ech051Constants.INSURER_REF_VEHICLE, result.getVehiclePersonLinks().getFirst().getInsurerRef());
  }

  @Test
  void shouldBuildTiersRelations() {
    List<Person> persons = List.of(person("P1"), person("P2"));
    List<Event> events = List.of(event("E1"));
    List<ObjectItem> objects = List.of(object("O1"));
    List<VehicleItem> vehicles = List.of(vehicle("V1"));
    BusinessCase businessCase = businessCase("B1");

    Relations result = mapper.buildRelations(persons, events, objects, vehicles, businessCase, DeclarationType.TIERS, null);

    assertNotNull(result);
    assertEquals(1, size(result.getPersonLinks()));
    assertEquals(2, size(result.getInvolvedParties()));
    assertEquals(1, size(result.getEventBusinessCaseLinks()));
    assertEquals(1, size(result.getEventObjectLinks()));
    assertEquals(4, size(result.getObjectPersonLinks()));
    assertEquals(1, size(result.getEventVehicleLinks()));
    assertEquals(4, size(result.getVehiclePersonLinks()));

    assertEquals("P2", result.getPersonLinks().getFirst().getPerson1Ref());
    assertEquals("P1", result.getPersonLinks().getFirst().getPerson2Ref());

    assertTrue(result.getInvolvedParties().stream().anyMatch(i -> "P1".equals(i.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream().anyMatch(i -> "P2".equals(i.getPersonRef())));
  }

  @Test
  void shouldBuildEntrepriseRelations() {
    List<Person> persons = List.of(person("ORG1"), person("DEC1"), person("INF1"));
    List<Event> events = List.of(event("E1"));
    List<ObjectItem> objects = List.of(object("O1"));
    List<VehicleItem> vehicles = List.of(vehicle("V1"));
    BusinessCase businessCase = businessCase("B1");

    Relations result = mapper.buildRelations(persons, events, objects, vehicles, businessCase, DeclarationType.ENTREPRISE, null);

    assertNotNull(result);
    assertEquals(2, size(result.getPersonLinks()));
    assertEquals(3, size(result.getInvolvedParties()));
    assertEquals(1, size(result.getEventBusinessCaseLinks()));
    assertEquals(1, size(result.getEventObjectLinks()));
    assertEquals(1, size(result.getObjectPersonLinks()));
    assertEquals(1, size(result.getEventVehicleLinks()));
    assertEquals(1, size(result.getVehiclePersonLinks()));

    assertTrue(result.getPersonLinks().stream().anyMatch(p -> "DEC1".equals(p.getPerson1Ref()) && "ORG1".equals(p.getPerson2Ref())));
    assertTrue(result.getPersonLinks().stream().anyMatch(p -> "INF1".equals(p.getPerson1Ref()) && "ORG1".equals(p.getPerson2Ref())));

    assertTrue(result.getInvolvedParties().stream().anyMatch(i -> "ORG1".equals(i.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream().anyMatch(i -> "DEC1".equals(i.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream().anyMatch(i -> "INF1".equals(i.getPersonRef())));

    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, result.getObjectPersonLinks().getFirst().getInsurerRef());
    assertEquals("DEC1", result.getObjectPersonLinks().getFirst().getPersonRef());

    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, result.getVehiclePersonLinks().getFirst().getInsurerRef());
    assertEquals("ORG1", result.getVehiclePersonLinks().getFirst().getPersonRef());
  }

  @Test
  void shouldHandleMissingDataWithoutCreatingInvalidLinks() {
    Relations result = mapper.buildRelations(List.of(), List.of(), List.of(object("O1")), List.of(vehicle("V1")), null, DeclarationType.INDIVIDUAL, null);

    assertNotNull(result);
    assertEquals(0, size(result.getPersonLinks()));
    assertEquals(0, size(result.getInvolvedParties()));
    assertEquals(0, size(result.getEventBusinessCaseLinks()));
    assertEquals(0, size(result.getEventObjectLinks()));
    assertEquals(0, size(result.getObjectPersonLinks()));
    assertEquals(0, size(result.getEventVehicleLinks()));
    assertEquals(0, size(result.getVehiclePersonLinks()));
  }

  @Test
  void shouldHandleEntrepriseWithoutInformant() {
    List<Person> persons = List.of(person("ORG1"), person("DEC1"));

    Relations result = mapper.buildRelations(persons, List.of(event("E1")), List.of(object("O1")), List.of(vehicle("V1")), businessCase("B1"), DeclarationType.ENTREPRISE, null);

    assertNotNull(result);
    assertEquals(1, size(result.getPersonLinks()));
    assertEquals(2, size(result.getInvolvedParties()));
    assertEquals(1, size(result.getObjectPersonLinks()));
    assertEquals(1, size(result.getVehiclePersonLinks()));
  }

  @Test
  void shouldBuildFinancialTransactionForAchatNonRecu() {
    Person victim = person(Ech051Constants.PERSON_KEY_TIERS);
    Person accused = Person.builder()
        .key("8")
        .naturalIdentity(NaturalIdentity.builder().key("8").identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN).build())
        .build();
    List<Person> persons = List.of(victim, accused);
    List<Event> events = List.of(event("E1"));
    BusinessCase businessCase = businessCase("B1");

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setPlateformeUtilisee(PlateformeUtilisee.RICARDO);
    achat.setPlateformeId("RID-778899");
    achat.setIbanBeneficiaire("CH93-0076-2011-6238-5295-7");
    achat.setDateOperation("2026-03-16");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    Relations result = mapper.buildRelations(persons, events, List.of(), List.of(), businessCase, DeclarationType.INDIVIDUAL, cybercrime);

    assertNotNull(result);
    assertEquals(1, size(result.getFinancialTransactions()));
    assertEquals(0, size(result.getEventObjectLinks()));
    assertEquals(0, size(result.getObjectPersonLinks()));
    assertEquals("IBAN", result.getFinancialTransactions().getFirst().getPaymentType());
    assertNull(result.getFinancialTransactions().getFirst().getTransactionNumber());
    assertEquals("Ricardo", result.getFinancialTransactions().getFirst().getPlatformType());
    assertEquals("RID-778899", result.getFinancialTransactions().getFirst().getPlatformId());
    assertEquals("inconnu", result.getFinancialTransactions().getFirst().getAccountSend());
    assertEquals("CH93-0076-2011-6238-5295-7", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("E1", result.getFinancialTransactions().getFirst().getEventRef());
    assertEquals(Ech051Constants.PERSON_KEY_TIERS, result.getFinancialTransactions().getFirst().getPersonSendRef());
    assertEquals("8", result.getFinancialTransactions().getFirst().getPersonReceiveRef());
  }

  @Test
  void shouldBuildFinancialTransactionForPaypal() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.PAYPAL, achat -> {
      achat.setComptePaypalBeneficiaire("buyer@paypal.test");
    });
    assertEquals("buyer@paypal.test", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("buyer@paypal.test", result.getFinancialTransactions().getFirst().getTransactionNumber());
  }

  @Test
  void shouldBuildFinancialTransactionForTwint() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.TWINT, achat -> {
      achat.setNumeroTwintBeneficiaire("+41790000000");
    });
    assertEquals("+41790000000", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("+41790000000", result.getFinancialTransactions().getFirst().getTransactionNumber());
  }

  @Test
  void shouldBuildFinancialTransactionForCrypto() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.CRYPTO, achat -> {
      achat.setAdresseWalletCrypto("0xabc");
      achat.setMontantDelitAchatLigne("1.5");
      achat.setHashTransactionCrypto("0xhash");
    });
    assertEquals("0xabc", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("1.5", result.getFinancialTransactions().getFirst().getCryptoCurrencyUnits());
    assertEquals("0xhash", result.getFinancialTransactions().getFirst().getTransactionNumber());
  }

  @Test
  void shouldBuildFinancialTransactionForAutre() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.AUTRE, achat -> {
      achat.setMoyenPaiementAutre("virement express");
    });
    assertEquals("virement express", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("virement express", result.getFinancialTransactions().getFirst().getPaymentType());
  }

  @Test
  void shouldUseLiteralAutreWhenMoyenPaiementAutreBlank() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.AUTRE, achat -> {
      achat.setMoyenPaiementAutre(null);
    });
    assertEquals("AUTRE", result.getFinancialTransactions().getFirst().getPaymentType());
  }

  @Test
  void shouldPreserveRawDateWhenDateOperationNotIsoDate() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.IBAN, achat -> {
      achat.setIbanBeneficiaire("CH9300762011623852957");
      achat.setDateOperation("2026-03-16T12:00:00+01:00");
    });
    assertEquals("2026-03-16T12:00:00+01:00", result.getFinancialTransactions().getFirst().getPaymentDateTime());
  }

  @Test
  void shouldOmitAccusedWhenLegalCounterpartyNameIsAucune() {
    Person victim = person(Ech051Constants.PERSON_KEY_TIERS);
    Person notAccused = Person.builder()
        .key("9")
        .type(PersonType.LEGAL)
        .legalIdentity(LegalIdentity.builder().currentName("aucune").build())
        .build();
    List<Person> persons = List.of(victim, notAccused);
    List<Event> events = List.of(event("E1"));
    BusinessCase businessCase = businessCase("B1");

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setPlateformeUtilisee(PlateformeUtilisee.ANIBIS);
    achat.setPlateformeId("X");
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2026-03-16");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    Relations result = mapper.buildRelations(persons, events, List.of(), List.of(), businessCase, DeclarationType.INDIVIDUAL, cybercrime);

    assertEquals(1, size(result.getFinancialTransactions()));
    assertNull(result.getFinancialTransactions().getFirst().getPersonReceiveRef());
  }

  private Relations buildFinancialTransactionRelations(
      MoyenPaiement moyen,
      java.util.function.Consumer<AchatNonRecu> customizeAchat
  ) {
    Person victim = person(Ech051Constants.PERSON_KEY_TIERS);
    Person accused = Person.builder()
        .key("8")
        .naturalIdentity(NaturalIdentity.builder().key("8").identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN).build())
        .build();
    List<Person> persons = List.of(victim, accused);
    List<Event> events = List.of(event("E1"));
    BusinessCase businessCase = businessCase("B1");

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(moyen);
    achat.setPlateformeUtilisee(PlateformeUtilisee.RICARDO);
    achat.setPlateformeId("RID-1");
    achat.setDateOperation("2026-03-16");
    customizeAchat.accept(achat);

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    return mapper.buildRelations(persons, events, List.of(), List.of(), businessCase, DeclarationType.INDIVIDUAL, cybercrime);
  }

  private static Person person(String key) {
    return Person.builder().key(key).build();
  }

  private static Event event(String key) {
    return Event.builder().key(key).build();
  }

  private static ObjectItem object(String key) {
    return ObjectItem.builder().key(key).build();
  }

  private static VehicleItem vehicle(String key) {
    return VehicleItem.builder().key(key).build();
  }

  private static BusinessCase businessCase(String key) {
    return BusinessCase.builder().key(key).build();
  }

  private static int size(List<?> list) {
    return list == null ? 0 : list.size();
  }
}
