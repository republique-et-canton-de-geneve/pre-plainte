package ch.ge.police.infrastructure.ech051;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.mapper.SuisseEpoliceMapperForPPL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Ech051BuilderTest {

  @Mock
  private SuisseEpoliceMapperForPPL mapper;

  private Ech051Builder builder;

  @BeforeEach
  void setUp() {
    builder = new Ech051Builder(mapper);

    ReflectionTestUtils.setField(builder, "senderId", "T4-TEST");
    ReflectionTestUtils.setField(builder, "recipientId", "T4-DEST");
    ReflectionTestUtils.setField(builder, "defaultMessageType", "9999");
    ReflectionTestUtils.setField(builder, "action", "1");
    ReflectionTestUtils.setField(builder, "manufacturer", "Test");
    ReflectionTestUtils.setField(builder, "product", "Builder");
    ReflectionTestUtils.setField(builder, "productVersion", "1.0");
  }

  @Test
  void generateEch051Xml_shouldProduceCompleteXmlAndCoverMissingBranches() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    when(mapper.toDocument(prePlainte)).thenReturn(buildCompletePayload());

    String xml = builder.generateEch051Xml(prePlainte, true);

    assertThat(xml).contains("<eCH-0058:senderId>T4-TEST</eCH-0058:senderId>").contains("<eCH-0058:recipientId>T4-DEST</eCH-0058:recipientId>").contains("<eCH-0058:testDeliveryFlag>true</eCH-0058:testDeliveryFlag>").contains("<eCH-0051:legal>").contains("<eCH-0051:currentName>ACME Assurance SA</eCH-0051:currentName>").contains("john.doe@test.ch").contains("+41791234567").contains("+41221234567").contains("<eCH-0051:uri>").contains("<eCH-0051:marking xml:lang=\"fr\">quality_assurance_link</eCH-0051:marking>").contains("<eCH-0051:uri>https://example.test/profile</eCH-0051:uri>").contains("<eCH-0051:bootyAmount>").contains("<eCH-0051:amount>2500</eCH-0051:amount>").contains("sourceTable=\"LOCALITY_TABLE\"").contains("Quartier centre").contains("2026-01-12T10:15:00.000+01:00").contains("2026-01-12T11:45:00.000+01:00").contains("2026-01-13T00:00:00.000+01:00").contains("2026-01-14T00:00:00.000+01:00").contains("date-invalide").contains("<eCH-0051:vehicle>").contains("VELO-999").contains("CADRE-123").contains("VIN-456").contains("AutreMarque").contains("AutreModele").contains("GE123456").contains("sourceTable=\"CANTON_TABLE\"").contains("sourceTable=\"COUNTRY_TABLE\"").contains("<eCH-0051:vehiclePersonLink>").contains("VEH-1").contains("PER-1").contains("INS-1").contains("sourceTable=\"vehicleTypeTable\"").contains("sourceTable=\"vehicleBrandTable\"").contains("sourceTable=\"vehicleModelTable\"").contains("sourceTable=\"vehicleColourTable\"").contains("sourceTable=\"vehicleColourSecondaryTable\"").contains("<eCH-0051:financialTransaction").contains("sep:paymentType=\"IBAN\"").contains("sep:platformType=\"Ricardo\"").contains("sep:platformId=\"RID-778899\"").contains("sep:transactionNumber=\"REF-778899\"").contains("<eCH-0051:accountSend>inconnu</eCH-0051:accountSend>").contains("<eCH-0051:accountReceive>CH9300762011623852957</eCH-0051:accountReceive>");
  }

  @Test
  void generateEch051Xml_shouldUseVolMessageTypeWhenNoVehicleAndFalseTestFlag() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    when(mapper.toDocument(prePlainte)).thenReturn(buildPayloadWithoutVehicle());

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("<eCH-0058:testDeliveryFlag>false</eCH-0058:testDeliveryFlag>").contains("<eCH-0058:messageType>" + Ech051Constants.MessageTypes.VOL + "</eCH-0058:messageType>").contains("<eCH-0051:relations").doesNotContain("<eCH-0051:vehicle>");
  }

  @Test
  void generateEch051Xml_shouldUseCyberAchatMessageTypeWhenProcessDataMatches() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    when(mapper.toDocument(prePlainte)).thenReturn(buildPayloadWithCyberProcessSource(Ech051Constants.SourceIds.CYBERCRIME_ACHAT_NON_RECU));

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("<eCH-0058:messageType>" + Ech051Constants.MessageTypes.CYBER_ACHAT_NON_RECU + "</eCH-0058:messageType>");
  }

  @Test
  void generateEch051Xml_shouldUseCyberFausseAnnonceMessageTypeWhenProcessDataMatches() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    when(mapper.toDocument(prePlainte)).thenReturn(buildPayloadWithCyberProcessSource(Ech051Constants.SourceIds.CYBERCRIME_FAUSSE_ANNONCE));

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("<eCH-0058:messageType>" + Ech051Constants.MessageTypes.CYBER_FAUSSE_ANNONCE + "</eCH-0058:messageType>");
  }

  @Test
  void generateEch051Xml_shouldUseCyberCommandeMessageTypeWhenProcessDataMatches() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    when(mapper.toDocument(prePlainte)).thenReturn(buildPayloadWithCyberProcessSource(Ech051Constants.SourceIds.CYBERCRIME_COMMANDE_FRAUDULEUSE));

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("<eCH-0058:messageType>" + Ech051Constants.MessageTypes.CYBER_COMMANDE_FRAUDULEUSE + "</eCH-0058:messageType>");
  }

  @Test
  void generateEch051Xml_shouldMapAddressUsingAdditionalWhenStreetBlank() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    Ech0051DocumentPayload.Person person = Ech0051DocumentPayload.Person.builder()
        .key("P-ADD")
        .naturalIdentity(Ech0051DocumentPayload.NaturalIdentity.builder().key("N-1").officialName("X").firstName("Y").build())
        .address(Ech0051DocumentPayload.Address.builder()
            .street("")
            .additional("Rue sans numéro explicite 12")
            .place(Ech0051DocumentPayload.RipolLocation.builder().code("1200").label("Genève").sourceTable("EXT_GDE").zipCode("1200").build())
            .country(Ech0051DocumentPayload.RipolLocation.builder().code("8100").label("CH").sourceTable("COUNTRY_TABLE").build())
            .build())
        .build();

    Ech0051DocumentPayload payload = Ech0051DocumentPayload.builder()
        .processData(Ech0051DocumentPayload.ProcessData.builder().deliveryDate("2026-03-16").sourceId(Ech051Constants.SourceIds.VOL).processingStatus("GREEN").build())
        .persons(List.of(person))
        .events(List.of(buildEventWithDateTime()))
        .objects(List.of(buildObject()))
        .relations(Ech0051DocumentPayload.Relations.builder().build())
        .build();

    when(mapper.toDocument(prePlainte)).thenReturn(payload);

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("Rue sans numéro explicite").contains("<eCH-0051:houseNumber>12</eCH-0051:houseNumber>");
  }

  @Test
  void generateEch051Xml_shouldMapPersonWithCyberTechnicalFields() {
    PrePlainte prePlainte = mock(PrePlainte.class);
    Ech0051DocumentPayload.Person person = Ech0051DocumentPayload.Person.builder()
        .key("P-CYB")
        .naturalIdentity(Ech0051DocumentPayload.NaturalIdentity.builder().key("N-1").officialName("Z").firstName("W").build())
        .deliveredAbroad(true)
        .build();

    Ech0051DocumentPayload payload = Ech0051DocumentPayload.builder()
        .processData(Ech0051DocumentPayload.ProcessData.builder().deliveryDate("2026-03-16").sourceId(Ech051Constants.SourceIds.VOL).processingStatus(null).build())
        .persons(List.of(person))
        .events(List.of(buildEventWithDateTime()))
        .objects(List.of(buildObject()))
        .relations(Ech0051DocumentPayload.Relations.builder().build())
        .build();

    when(mapper.toDocument(prePlainte)).thenReturn(payload);

    String xml = builder.generateEch051Xml(prePlainte, false);

    assertThat(xml).contains("deliveredAbroad");
  }

  private Ech0051DocumentPayload buildPayloadWithCyberProcessSource(String sourceId) {
    return Ech0051DocumentPayload.builder()
        .processData(Ech0051DocumentPayload.ProcessData.builder()
            .deliveryDate("2026-03-16T09:15:00+01:00")
            .sourceId(sourceId)
            .processingStatus("GREEN")
            .build())
        .persons(List.of(buildNaturalPerson()))
        .events(List.of(buildEventWithDateTime()))
        .objects(List.of(buildObject()))
        .relations(Ech0051DocumentPayload.Relations.builder().build())
        .build();
  }

  private Ech0051DocumentPayload buildCompletePayload() {
    return Ech0051DocumentPayload.builder().processData(Ech0051DocumentPayload.ProcessData.builder().deliveryDate("2026-03-16T09:15:00+01:00").sourceId("PPL_TABLE").processingStatus("RED").build()).persons(List.of(buildNaturalPerson(), buildLegalPerson())).events(List.of(buildEventWithDateTime(), buildEventWithDateOnly(), buildEventWithInvalidDate())).objects(List.of(buildObject())).vehicles(List.of(buildVehicle())).businessCases(List.of(buildBusinessCase())).relations(buildRelations()).build();
  }

  private Ech0051DocumentPayload buildPayloadWithoutVehicle() {
    return Ech0051DocumentPayload.builder().processData(Ech0051DocumentPayload.ProcessData.builder().deliveryDate("2026-03-16T09:15:00+01:00").sourceId("PPL_TABLE").processingStatus("RED").build()).persons(List.of(buildNaturalPerson())).events(List.of(buildEventWithDateTime())).objects(List.of(buildObject())).relations(Ech0051DocumentPayload.Relations.builder().build()).build();
  }

  private Ech0051DocumentPayload.Person buildNaturalPerson() {
    return Ech0051DocumentPayload.Person.builder().key("PER-1").naturalIdentity(Ech0051DocumentPayload.NaturalIdentity.builder().key("NAT-1").officialName("DOE").originalName("DOE").firstName("John").callName("Johnny").birthDate("1990-01-01").languageCode("fr").profession("Développeur").additionalInformation("Info complémentaire").socialSecurityNumber("756.1234.5678.97").sex(ripolRef("1", "masculin", "RIPOL", "ISO5218")).nationality(ripolRef("8100", "Suisse", "RIPOL", "EXT_GPNATI")).placeOfOrigin(Ech0051DocumentPayload.RipolLocation.builder().code("543").label("Sumiswald").sourceTable("EXT_GDE_HEIMATORT").zipCode("3454").build()).build()).address(Ech0051DocumentPayload.Address.builder().street("Rue du Rhône 42").houseNumber(null).place(Ech0051DocumentPayload.RipolLocation.builder().code("1204").label("Genève").sourceTable("EXT_GDE").zipCode("1204").build()).country(Ech0051DocumentPayload.RipolLocation.builder().code("8100").label("Suisse").sourceTable("COUNTRY_TABLE").build()).build()).communication(Ech0051DocumentPayload.Communication.builder().email("john.doe@test.ch").mobile("+41791234567").phone("+41221234567").uri("https://example.test/profile").build()).additionalInformation("Victime principale").build();
  }

  private Ech0051DocumentPayload.Person buildLegalPerson() {
    return Ech0051DocumentPayload.Person.builder().key("PER-LEGAL-1").legalIdentity(Ech0051DocumentPayload.LegalIdentity.builder().currentName("ACME Assurance SA").build()).additionalInformation("Assureur").build();
  }

  private Ech0051DocumentPayload.Event buildEventWithDateTime() {
    return Ech0051DocumentPayload.Event.builder().key("EVT-1").descriptionShort("Vol simple").complaintDate("2026-01-12T12:00:00+01:00").bootyAmount("2500").additionalInformation("Complément événement").locality(ripolRef("CENTRE", "Quartier centre", "RIPOL", "LOCALITY_TABLE")).modeOperandi(ripolRef("MO-1", "Effraction", "RIPOL", "MODE_TABLE")).typeOfCrime(ripolRef("CR-1", "Vol", "RIPOL", "CRIME_TABLE")).actionPlace(Ech0051DocumentPayload.ActionPlace.builder().street("Rue de Lausanne 15").place(Ech0051DocumentPayload.RipolLocation.builder().code("1201").label("Genève").sourceTable("EXT_GDE").zipCode("1201").build()).cityArea("Pâquis").build()).actionPeriod(Ech0051DocumentPayload.ActionPeriod.builder().from("2026-01-12T10:15").to("2026-01-12T11:45").build()).build();
  }

  private Ech0051DocumentPayload.Event buildEventWithDateOnly() {
    return Ech0051DocumentPayload.Event.builder().key("EVT-2").descriptionShort("Vol date seule").complaintDate("2026-01-13T12:00:00+01:00").actionPeriod(Ech0051DocumentPayload.ActionPeriod.builder().from("2026-01-13").to("2026-01-14").build()).build();
  }

  private Ech0051DocumentPayload.Event buildEventWithInvalidDate() {
    return Ech0051DocumentPayload.Event.builder().key("EVT-3").descriptionShort("Vol date invalide").complaintDate("2026-01-15T12:00:00+01:00").actionPeriod(Ech0051DocumentPayload.ActionPeriod.builder().from("date-invalide").to("").build()).build();
  }

  private Ech0051DocumentPayload.ObjectItem buildObject() {
    return Ech0051DocumentPayload.ObjectItem.builder().key("OBJ-1").additionalInformation("Objet volé").gravure("gravure").fabricantAutre("Autre fabricant").modeleAutre("Autre modèle").typeOfObject(ripolRef("713103", "Téléphone mobile", "RIPOL", "sacheBezeichnung")).fabricant(ripolRef("4865", "Apple", "RIPOL", "sacheMarke")).modele(ripolRef("224124", "iPhone XS Max", "RIPOL", "sacheModell")).material("Aluminium").couleur(ripolRef("1101", "beige-brun", "RIPOL", "sacheFarbe")).couleurSecondaire(ripolRef("1102", "noir", "RIPOL", "sacheFarbe")).realValue("1500").purchaseDate("2023-06-15").identification(Ech0051DocumentPayload.Identification.builder().type("imei").number("353456789012345").build()).build();
  }

  private Ech0051DocumentPayload.VehicleItem buildVehicle() {
    return Ech0051DocumentPayload.VehicleItem.builder().key("VEH-1").additionalInformation("Vélo électrique").velofinderId("VELO-999").purchaseDate("2024-02-10").frameNumber("CADRE-123").markOther("AutreMarque").modelOther("AutreModele").vin("VIN-456").typeOfVehicle(ripolRef("BIKE", "Vélo", "RIPOL", "vehicleTypeTable")).mark(ripolRef("TREK", "Trek", "RIPOL", "vehicleBrandTable")).modelType(ripolRef("DOMANE", "Domane", "RIPOL", "vehicleModelTable")).colour(ripolRef("BLUE", "Bleu", "RIPOL", "vehicleColourTable")).colourSecondary(ripolRef("BLACK", "Noir", "RIPOL", "vehicleColourSecondaryTable")).numberPlate(Ech0051DocumentPayload.NumberPlate.builder().number("GE123456").country(Ech0051DocumentPayload.RipolLocation.builder().code("8100").label("Suisse").sourceTable("COUNTRY_TABLE").build()).canton(Ech0051DocumentPayload.RipolLocation.builder().code("GE").label("Genève").sourceTable("CANTON_TABLE").build()).build()).build();
  }

  private Ech0051DocumentPayload.BusinessCase buildBusinessCase() {
    return Ech0051DocumentPayload.BusinessCase.builder().key("BC-1").caseNumber("CASE-2026-0001").file(List.of(Ech0051DocumentPayload.File.builder().attachment(List.of(Ech0051DocumentPayload.Attachment.builder().filename("piece-jointe.pdf").content("JVBERi0xLjQK").build())).build())).build();
  }

  private Ech0051DocumentPayload.Relations buildRelations() {
    return Ech0051DocumentPayload.Relations.builder()
        .vehiclePersonLinks(List.of(Ech0051DocumentPayload.VehiclePersonLink.builder().vehicleRef("VEH-1").personRef("PER-1").insurerRef("INS-1").personRole(ripolRef("OWNER", "Propriétaire", "RIPOL", "ROLE_TABLE")).build()))
        .financialTransactions(List.of(Ech0051DocumentPayload.FinancialTransaction.builder()
            .paymentType("IBAN")
            .transactionNumber("REF-778899")
            .platformType("Ricardo")
            .platformId("RID-778899")
            .paymentDateTime("2026-03-16T00:00:00.000+01:00")
            .paymentDateTimeCirca("false")
            .accountSend("inconnu")
            .accountReceive("CH9300762011623852957")
            .eventRef("EVT-1")
            .personSendRef("PER-1")
            .personReceiveRef("PER-LEGAL-1")
            .build()))
        .build();
  }

  private Ech0051DocumentPayload.RipolReference ripolRef(String code, String label, String source, String sourceTable) {
    return Ech0051DocumentPayload.RipolReference.builder().code(code).label(label).source(source).sourceTable(sourceTable).build();
  }
}
