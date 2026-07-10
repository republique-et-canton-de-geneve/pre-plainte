package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.MoyenPaiement;
import ch.ge.police.core.domain.model.event.cybercrime.common.PlateformeUtilisee;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.FinancialTransaction;
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
  void shouldBuildIndividualRelationsWithVehicleUsingPhysicalPersonSchema() {
    List<Person> persons = List.of(
        person(Ech051Constants.PERSON_KEY_TIERS),
        person(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE),
        person(Ech051Constants.PERSON_KEY_INFORMANT),
        person(Ech051Constants.INSURER_KEY_VEHICLE)
    );
    List<Event> events = List.of(event("E1"));
    List<ObjectItem> objects = List.of(object("O1"));
    List<VehicleItem> vehicles = List.of(vehicle("V1"));
    BusinessCase businessCase = businessCase("B1");

    Relations result = mapper.buildRelations(persons, events, objects, vehicles, businessCase, DeclarationType.INDIVIDUAL, null);

    assertNotNull(result);
    assertEquals(2, size(result.getPersonLinks()));
    assertEquals(3, size(result.getInvolvedParties()));
    assertEquals(1, size(result.getObjectPersonLinks()));
    assertEquals(1, size(result.getVehiclePersonLinks()));

    assertEquals(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE, result.getPersonLinks().getFirst().getPerson1Ref());
    assertEquals(Ech051Constants.PERSON_KEY_TIERS, result.getPersonLinks().getFirst().getPerson2Ref());

    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(i -> Ech051Constants.PERSON_KEY_TIERS.equals(i.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(i -> Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(i.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(i -> Ech051Constants.PERSON_KEY_INFORMANT.equals(i.getPersonRef())));

    assertEquals(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE, result.getObjectPersonLinks().getFirst().getPersonRef());
    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, result.getObjectPersonLinks().getFirst().getInsurerRef());

    assertEquals(Ech051Constants.PERSON_KEY_TIERS, result.getVehiclePersonLinks().getFirst().getPersonRef());
    assertEquals(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, result.getVehiclePersonLinks().getFirst().getInsurerRef());
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

    assertTrue(result.getObjectPersonLinks().stream()
        .allMatch(link -> Ech051Constants.INSURER_REF_VEHICLE.equals(link.getInsurerRef())));
    assertTrue(result.getVehiclePersonLinks().stream()
        .allMatch(link -> Ech051Constants.INSURER_REF_VEHICLE.equals(link.getInsurerRef())));
  }

  @Test
  void shouldUseDefaultInsurerRefForTiersWithoutVehicle() {
    List<Person> persons = List.of(person("P1"), person("P2"));
    List<Event> events = List.of(event("E1"));
    List<ObjectItem> objects = List.of(object("O1"));

    Relations result = mapper.buildRelations(
        persons, events, objects, List.of(), businessCase("B1"), DeclarationType.TIERS, null);

    assertNotNull(result);
    assertTrue(result.getObjectPersonLinks().stream()
        .allMatch(link -> Ech051Constants.INSURER_REF.equals(link.getInsurerRef())));
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
  void shouldBuildEntrepriseRepresentativeRelationsForCyberAchatNonRecu() {
    List<Person> persons = List.of(
        person(Ech051Constants.PERSON_KEY_ORGANISATION),
        person(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE),
        person(Ech051Constants.PERSON_KEY_INFORMANT),
        Person.builder()
            .key("9")
            .naturalIdentity(NaturalIdentity.builder()
                .key("9")
                .identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN)
                .build())
            .build()
    );
    List<Event> events = achatNonRecuEvents();
    BusinessCase businessCase = businessCase("B1");

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2026-03-16");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    Relations result = mapper.buildRelations(
        persons, events, List.of(), List.of(), businessCase, DeclarationType.ENTREPRISE, cybercrime);

    assertTrue(result.getPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(link.getPerson1Ref())
            && Ech051Constants.PERSON_KEY_ORGANISATION.equals(link.getPerson2Ref())));
    assertTrue(result.getPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.PERSON_KEY_INFORMANT.equals(link.getPerson1Ref())
            && Ech051Constants.PERSON_KEY_ORGANISATION.equals(link.getPerson2Ref())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(party -> Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(party.getPersonRef())
            && Ech051Constants.EVENT_KEY.equals(party.getEventRef())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(party -> Ech051Constants.PERSON_KEY_INFORMANT.equals(party.getPersonRef())
            && Ech051Constants.EVENT_KEY.equals(party.getEventRef())));
  }

  @Test
  void shouldLinkCyberIdentityObjectToEntrepriseDeclarant() {
    List<Person> persons = List.of(
        person(Ech051Constants.PERSON_KEY_ORGANISATION),
        Person.builder()
            .key(Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE)
            .naturalIdentity(NaturalIdentity.builder()
                .key(Ech051Constants.IDENTITY_KEY_DECLARANT_ENTREPRISE)
                .build())
            .build(),
        person(Ech051Constants.PERSON_KEY_INFORMANT),
        Person.builder()
            .key("9")
            .naturalIdentity(NaturalIdentity.builder()
                .key("9")
                .identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN)
                .build())
            .build()
    );
    ObjectItem identity = ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY)
        .build();
    ObjectItem undelivered = ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_UNDELIVERED_ITEM)
        .description("Article non livré")
        .realValue("2342")
        .build();

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2026-07-08");
    achat.setMontantDelitAchatLigne("2342");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    Relations result = mapper.buildRelations(
        persons,
        achatNonRecuEvents(),
        List.of(identity, undelivered),
        List.of(),
        businessCase("B1"),
        DeclarationType.ENTREPRISE,
        cybercrime
    );

    assertTrue(result.getObjectPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(link.getPersonRef())
            && link.getPersonRole() == null));
    assertTrue(result.getObjectPersonLinks().stream()
        .noneMatch(link -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && Ech051Constants.PERSON_KEY_ORGANISATION.equals(link.getPersonRef())));
  }

  @Test
  void shouldBuildTiersRepresentativeRelationsForCyberFausseAnnonce() {
    Person tier = person(Ech051Constants.PERSON_KEY_TIERS);
    Person declarant = person(Ech051Constants.PERSON_KEY_DECLARANT);

    FausseAnnonce annonce = new FausseAnnonce();
    annonce.setNomBailleur("Arnaqueur");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.FAUSSE_ANNONCE);
    cybercrime.setFausseAnnonce(annonce);

    Relations result = mapper.buildRelations(
        List.of(tier, declarant),
        List.of(event(Ech051Constants.EVENT_KEY)),
        List.of(),
        List.of(),
        businessCase("B1"),
        DeclarationType.TIERS,
        cybercrime
    );

    assertTrue(result.getPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.PERSON_KEY_DECLARANT.equals(link.getPerson1Ref())
            && Ech051Constants.PERSON_KEY_TIERS.equals(link.getPerson2Ref())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(party -> Ech051Constants.PERSON_KEY_TIERS.equals(party.getPersonRef())));
    assertTrue(result.getInvolvedParties().stream()
        .anyMatch(party -> Ech051Constants.PERSON_KEY_DECLARANT.equals(party.getPersonRef())));
  }

  @Test
  void shouldBuildFinancialTransactionForAchatNonRecu() {
    Person victim = person(Ech051Constants.PERSON_KEY_TIERS);
    Person accused = Person.builder()
        .key("8")
        .naturalIdentity(NaturalIdentity.builder().key("8").identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN).build())
        .build();
    List<Person> persons = List.of(victim, accused);
    List<Event> events = achatNonRecuEvents();
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
    assertNull(result.getFinancialTransactions().getFirst().getPlatformType());
    assertNull(result.getFinancialTransactions().getFirst().getPlatformId());
    assertEquals("Inconnu", result.getFinancialTransactions().getFirst().getAccountSend());
    assertEquals("CH93-0076-2011-6238-5295-7", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals(Ech051Constants.EVENT_KEY_PAYMENT, result.getFinancialTransactions().getFirst().getEventRef());
    assertEquals(Ech051Constants.PERSON_KEY_TIERS, result.getFinancialTransactions().getFirst().getPersonSendRef());
    assertNull(result.getFinancialTransactions().getFirst().getPersonReceiveRef());
  }

  @Test
  void shouldBuildFinancialTransactionForPaypal() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.PAYPAL, achat -> {
      achat.setComptePaypalBeneficiaire("adresse-email@beneficiaire.test");
      achat.setNumeroTransactionPaypal("074343243");
    });
    assertEquals("adresse-email@beneficiaire.test", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertEquals("074343243", result.getFinancialTransactions().getFirst().getTransactionNumber());
    assertEquals("PAYPAL", result.getFinancialTransactions().getFirst().getPaymentType());
  }

  @Test
  void shouldBuildFinancialTransactionForTwint() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.TWINT, achat -> {
      achat.setNumeroTwintBeneficiaire("+41790000000");
    });
    assertEquals("+41790000000", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertNull(result.getFinancialTransactions().getFirst().getTransactionNumber());
  }

  @Test
  void shouldBuildFinancialTransactionForCrypto() {
    Relations result = buildFinancialTransactionRelations(MoyenPaiement.CRYPTO, achat -> {
      achat.setTypeCryptoMonnaie("Bitcoin");
      achat.setMontantUnitesCrypto("20.00000");
      achat.setAdresseWalletExpediteur("wallet-expediteur");
      achat.setAdresseWalletCrypto("wallet-destinataire");
      achat.setHashTransactionCrypto("0xhash");
    });
    FinancialTransaction tx = result.getFinancialTransactions().getFirst();
    assertEquals(Ech051Constants.PAYMENT_TYPE_CRYPTO, tx.getPaymentType());
    assertEquals("wallet-expediteur", tx.getAccountSend());
    assertEquals("wallet-destinataire", tx.getAccountReceive());
    assertEquals("Bitcoin", tx.getCryptoCurrency());
    assertEquals("20.00000", tx.getCryptoCurrencyUnits());
    assertNull(tx.getTransactionNumber());
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
    List<Event> events = achatNonRecuEvents();
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

  @Test
  void shouldBuildAchatNonRecuObjectAndBeneficiaryLinks() {
    Person victim = person(Ech051Constants.PERSON_KEY_TIERS);
    Person seller = Person.builder()
        .key("8")
        .naturalIdentity(NaturalIdentity.builder().key("7").identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN).build())
        .build();
    Person beneficiary = Person.builder()
        .key("10")
        .naturalIdentity(NaturalIdentity.builder().key("9").identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN).build())
        .build();
    ObjectItem undelivered = ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_UNDELIVERED_ITEM)
        .description("Article non livré")
        .realValue("44343")
        .build();
    ObjectItem identity = ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY)
        .build();

    AchatNonRecu achat = new AchatNonRecu();
    achat.setMoyenPaiement(MoyenPaiement.TWINT);
    achat.setNumeroTwintBeneficiaire("0791234567");
    achat.setDateOperation("2026-07-01");

    Cybercrime cybercrime = new Cybercrime();
    cybercrime.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cybercrime.setAchatNonRecu(achat);

    Relations result = mapper.buildRelations(
        List.of(victim, seller, beneficiary),
        achatNonRecuEvents(),
        List.of(undelivered, identity),
        List.of(),
        businessCase("B1"),
        DeclarationType.INDIVIDUAL,
        cybercrime
    );

    assertEquals(2, size(result.getEventBusinessCaseLinks()));
    assertEquals(1, size(result.getEventObjectLinks()));
    assertEquals(Ech051Constants.EVENT_KEY, result.getEventObjectLinks().getFirst().getEventRef());
    assertEquals(2, size(result.getObjectPersonLinks()));
    assertTrue(result.getObjectPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && link.getPersonRole() == null));
    assertNull(result.getFinancialTransactions().getFirst().getPersonReceiveRef());
    assertEquals(Ech051Constants.EVENT_KEY_PAYMENT, result.getFinancialTransactions().getFirst().getEventRef());
    assertEquals("0791234567", result.getFinancialTransactions().getFirst().getAccountReceive());
    assertNull(result.getFinancialTransactions().getFirst().getTransactionNumber());
    assertTrue(result.getPersonLinks().isEmpty());
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
    List<Event> events = achatNonRecuEvents();
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

  private static List<Event> achatNonRecuEvents() {
    return List.of(event(Ech051Constants.EVENT_KEY), event(Ech051Constants.EVENT_KEY_PAYMENT));
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
