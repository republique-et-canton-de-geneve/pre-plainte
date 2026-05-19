package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ActionPlace;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ProcessData;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.MODUS_OPERANDI;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_CRIME;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_LIEU;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuisseEpoliceEventMapperTest {

  private SuisseEpoliceAddressMapper addressMapper;
  private SuisseEpoliceEventMapper mapper;

  @BeforeEach
  void setUp() {
    addressMapper = mock(SuisseEpoliceAddressMapper.class);
    mapper = new SuisseEpoliceEventMapper(addressMapper);
  }

  @Test
  void shouldBuildProcessDataForVol() {
    Vol vol = mock(Vol.class);

    ProcessData result = mapper.buildProcessData(vol, "SRC-001");

    assertEquals(LocalDate.now().toString(), result.getDeliveryDate());
    assertEquals(Ech051Constants.SourceIds.VOL, result.getSourceId());
    assertEquals(Ech051Constants.PROCESSING_STATUS_GREEN, result.getProcessingStatus());
  }

  @Test
  void shouldBuildProcessDataForDommageMateriel() {
    DommageMateriel dommage = mock(DommageMateriel.class);

    ProcessData result = mapper.buildProcessData(dommage, "SRC-001");

    assertEquals(Ech051Constants.SourceIds.DOMMAGE_MATERIEL, result.getSourceId());
  }

  @Test
  void shouldBuildProcessDataForCybercrime() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.ACHAT_NON_RECU);
    when(cybercrime.getCommandeFrauduleuse()).thenReturn(null);

    ProcessData result = mapper.buildProcessData(cybercrime, "SRC-001");

    assertEquals(Ech051Constants.SourceIds.CYBERCRIME_ACHAT_NON_RECU, result.getSourceId());
  }

  @Test
  void shouldBuildProcessDataFromTypeIncidentWhenNotSpecializedInstance() {
    IncidentBase incident = mock(IncidentBase.class);
    when(incident.getTypeIncident()).thenReturn(TypeIncident.VOL);

    ProcessData result = mapper.buildProcessData(incident, "SRC-001");

    assertEquals(Ech051Constants.SourceIds.VOL, result.getSourceId());
  }

  @Test
  void shouldBuildProcessDataUnknownWhenIncidentIsNull() {
    ProcessData result = mapper.buildProcessData(null, "SRC-001");

    assertEquals(Ech051Constants.SourceIds.UNKNOWN, result.getSourceId());
  }

  @Test
  void shouldBuildEventWithNullAddressFallback() {
    Vol incident = mock(Vol.class);
    when(incident.getAdresseIncident()).thenReturn(null);
    when(incident.getAdresseIncidentSecondaire()).thenReturn(null);
    when(incident.getDateDebutEvent()).thenReturn("2026-01-01");
    when(incident.getDateFinEvent()).thenReturn("2026-01-02");
    when(incident.getTypeIncident()).thenReturn(TypeIncident.VOL);
    when(incident.getTypeLieu()).thenReturn(null);

    Event result = mapper.buildEvent(incident, null);

    assertNotNull(result);
    assertEquals(Ech051Constants.EVENT_KEY, result.getKey());
    assertEquals(TypeIncident.VOL.jsonValue(), result.getDescriptionShort());
    assertEquals(LocalDate.now().toString(), result.getComplaintDate());
    assertEquals("2026-01-01", result.getActionPeriod().getFrom());
    assertEquals("2026-01-02", result.getActionPeriod().getTo());
    assertNull(result.getActionPlace());
    assertNull(result.getSecondaryActionPlace());
    assertNull(result.getBootyAmount());
    assertNull(result.getLocality());
    assertNull(result.getModeOperandi());
    assertNotNull(result.getTypeOfCrime());
    assertEquals(Ech051Constants.TYPE_OF_CRIME_VOL_CODE, result.getTypeOfCrime().getCode());
  }

  @Test
  void shouldBuildEventWithSecondaryActionPlace() {
    Vol incident = mock(Vol.class);
    InformationsPersonnelles infos = mock(InformationsPersonnelles.class);
    Adresse primaryAddress = mock(Adresse.class);
    Adresse secondaryAddress = mock(Adresse.class);

    RipolLocation primaryPlace = RipolLocation.builder()
      .code("405100")
      .label("Basel")
      .sourceTable("PTT_ORT")
      .zipCode("2494")
      .build();

    RipolLocation secondaryPlace = RipolLocation.builder()
      .code("120000")
      .label("Genève")
      .sourceTable("PTT_ORT")
      .zipCode("329")
      .build();

    when(incident.getAdresseIncident()).thenReturn(primaryAddress);
    when(incident.getAdresseIncidentSecondaire()).thenReturn(secondaryAddress);
    when(incident.getDateDebutEvent()).thenReturn("2026-03-10T10:00");
    when(incident.getDateFinEvent()).thenReturn("2026-03-10T12:00");
    when(incident.getTypeIncident()).thenReturn(TypeIncident.VOL);
    when(incident.getTypeLieu()).thenReturn(null);

    when(addressMapper.isAddressComplete(primaryAddress)).thenReturn(true);
    when(addressMapper.isAddressComplete(secondaryAddress)).thenReturn(true);

    when(primaryAddress.adresse()).thenReturn("SBB Bahnhof");
    when(primaryAddress.adressePostale()).thenReturn("Basel");
    when(addressMapper.buildAddressPlace(primaryAddress)).thenReturn(primaryPlace);

    when(secondaryAddress.adresse()).thenReturn("Gare Cornavin");
    when(secondaryAddress.adressePostale()).thenReturn("Genève");
    when(addressMapper.buildAddressPlace(secondaryAddress)).thenReturn(secondaryPlace);

    Event result = mapper.buildEvent(incident, infos);

    assertNotNull(result.getActionPlace());
    assertEquals("SBB Bahnhof", result.getActionPlace().getStreet());
    assertEquals("Basel", result.getActionPlace().getCityArea());
    assertEquals("405100", result.getActionPlace().getPlace().getCode());

    assertNotNull(result.getSecondaryActionPlace());
    assertEquals("Gare Cornavin", result.getSecondaryActionPlace().getStreet());
    assertEquals("Genève", result.getSecondaryActionPlace().getCityArea());
    assertEquals("120000", result.getSecondaryActionPlace().getPlace().getCode());
  }

  @Test
  void shouldBuildEventUsingPersonalAddressWhenIncidentAddressIncomplete() {
    IncidentBase incident = mock(IncidentBase.class);
    InformationsPersonnelles infos = mock(InformationsPersonnelles.class);
    Adresse incidentAddress = mock(Adresse.class);
    Adresse personalAddress = mock(Adresse.class);
    RipolLocation place = RipolLocation.builder().code("1200").label("Genève").sourceTable("LOCALITE").zipCode("1200").build();

    when(incident.getAdresseIncident()).thenReturn(incidentAddress);
    when(incident.getDateDebutEvent()).thenReturn("2026-01-01");
    when(incident.getDateFinEvent()).thenReturn("2026-01-02");
    when(incident.getTypeIncident()).thenReturn(TypeIncident.VOL);
    when(incident.getTypeLieu()).thenReturn(null);
    when(infos.getAdresse()).thenReturn(personalAddress);

    when(addressMapper.isAddressComplete(incidentAddress)).thenReturn(false);
    when(addressMapper.buildAddressPlace(personalAddress)).thenReturn(place);
    when(personalAddress.adresse()).thenReturn("Rue du Test 1");
    when(personalAddress.adressePostale()).thenReturn("Genève");

    Event result = mapper.buildEvent(incident, infos);

    assertNotNull(result.getActionPlace());
    assertEquals("Rue du Test 1", result.getActionPlace().getStreet());
    assertEquals("Genève", result.getActionPlace().getCityArea());
    assertEquals("1200", result.getActionPlace().getPlace().getCode());
  }

  @Test
  void shouldBuildCyberTransactionEventWithUnknownCountryAndContactPeriod() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    AchatNonRecu achat = new AchatNonRecu();

    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.ACHAT_NON_RECU);
    when(cybercrime.getAchatNonRecu()).thenReturn(achat);
    when(cybercrime.getCommandeFrauduleuse()).thenReturn(null);
    when(cybercrime.getFausseAnnonce()).thenReturn(null);
    when(cybercrime.getDatePremierContact()).thenReturn("2025-11-11T11:11:00.000+01:00");
    when(cybercrime.getDateDernierContact()).thenReturn("2025-11-11T12:00:00.000+01:00");
    when(cybercrime.getDescriptionCybercrime()).thenReturn("Faits");
    when(cybercrime.getTypeLieu()).thenReturn(null);

    Event result = mapper.buildEvent(cybercrime, null);

    assertNotNull(result.getActionPlace());
    assertNull(result.getActionPlace().getStreet());
    assertNull(result.getActionPlace().getPlace());
    assertNotNull(result.getActionPlace().getCountry());
    assertEquals(Ech051Constants.COUNTRY_UNKNOWN_RIPOL_CODE, result.getActionPlace().getCountry().getCode());
    assertEquals(Ech051Constants.COUNTRY_UNKNOWN_LABEL, result.getActionPlace().getCountry().getLabel());
    assertEquals("EXT_GPNATI", result.getActionPlace().getCountry().getSourceTable());

    assertEquals("2025-11-11T11:11:00.000+01:00", result.getActionPeriod().getFrom());
    assertEquals("2025-11-11T12:00:00.000+01:00", result.getActionPeriod().getTo());
    assertNull(result.getSecondaryActionPlace());
  }

  @Test
  void shouldBuildActionPlaceWithNullAdresse() {
    ActionPlace result = mapper.buildActionPlace(null);

    assertNull(result);
  }

  @Test
  void shouldBuildBootyAmountForDommageMateriel() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getMontantEstime()).thenReturn(1234.99);

    String result = mapper.buildBootyAmount(dommage);

    assertEquals("1234", result);
  }

  @Test
  void shouldReturnNullBootyAmountForOtherIncident() {
    IncidentBase incident = mock(IncidentBase.class);

    assertNull(mapper.buildBootyAmount(incident));
  }

  @Test
  void shouldBuildLocalityReference() {
    IncidentBase incident = mock(IncidentBase.class);
    var typeLieu = mock(RipolCode.class);

    when(incident.getTypeLieu()).thenReturn(typeLieu);
    when(typeLieu.code()).thenReturn("L1");
    when(typeLieu.label()).thenReturn("Appartement");

    RipolReference result = mapper.buildLocalityReference(incident);

    assertNotNull(result);
    assertEquals("L1", result.getCode());
    assertEquals("Appartement", result.getLabel());
    assertEquals(TYPE_LIEU, result.getSourceTable());
  }

  @Test
  void shouldReturnNullLocalityReferenceWhenMissingTypeLieu() {
    IncidentBase incident = mock(IncidentBase.class);
    when(incident.getTypeLieu()).thenReturn(null);

    assertNull(mapper.buildLocalityReference(incident));
  }

  @Test
  void shouldBuildModeOperandiReferenceForDegradations() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getNaturesDommage()).thenReturn(List.of(NatureDommage.DEGRADATIONS));

    RipolReference result = mapper.buildModeOperandiReference(dommage);

    assertNotNull(result);
    assertEquals(Ech051Constants.MODE_OPERANDI_DEGRADATIONS_CODE, result.getCode());
    assertEquals(Ech051Constants.MODE_OPERANDI_DEGRADATIONS_LABEL, result.getLabel());
    assertEquals(MODUS_OPERANDI, result.getSourceTable());
  }

  @Test
  void shouldBuildModeOperandiReferenceForTags() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getNaturesDommage()).thenReturn(List.of(NatureDommage.TAGS_GRAFFITI));

    RipolReference result = mapper.buildModeOperandiReference(dommage);

    assertNotNull(result);
    assertEquals(Ech051Constants.MODE_OPERANDI_TAGS_CODE, result.getCode());
    assertEquals(Ech051Constants.MODE_OPERANDI_TAGS_LABEL, result.getLabel());
    assertEquals(MODUS_OPERANDI, result.getSourceTable());
  }

  @Test
  void shouldReturnNullModeOperandiReferenceWhenNoSupportedNature() {
    IncidentBase incident = mock(IncidentBase.class);

    assertNull(mapper.buildModeOperandiReference(incident));

    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getNaturesDommage()).thenReturn(List.of());

    assertNull(mapper.buildModeOperandiReference(dommage));
  }

  @Test
  void shouldBuildTypeOfCrimeReference() {
    RipolReference volRef = mapper.buildTypeOfCrimeReference(mock(Vol.class));
    RipolReference dommageRef = mapper.buildTypeOfCrimeReference(mock(DommageMateriel.class));
    RipolReference otherRef = mapper.buildTypeOfCrimeReference(mock(IncidentBase.class));

    assertNotNull(volRef);
    assertEquals(Ech051Constants.TYPE_OF_CRIME_VOL_CODE, volRef.getCode());
    assertEquals(TYPE_CRIME, volRef.getSourceTable());

    assertNotNull(dommageRef);
    assertEquals(Ech051Constants.TYPE_OF_CRIME_DOMMAGE_CODE, dommageRef.getCode());
    assertEquals(TYPE_CRIME, dommageRef.getSourceTable());

    assertNull(otherRef);
  }

  @Test
  void shouldBuildEventAdditionalInformation() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getDescription()).thenReturn("Vitre cassée");
    assertEquals("Vitre cassée", mapper.buildEventAdditionalInformation(dommage));

    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getDescriptionCybercrime()).thenReturn("Hameçonnage");
    assertNull(mapper.buildEventAdditionalInformation(cybercrime));

    IncidentBase incident = mock(IncidentBase.class);
    assertNull(mapper.buildEventAdditionalInformation(incident));
    assertNull(mapper.buildEventAdditionalInformation(null));
  }

  @Test
  void shouldExtractIncidentTypeValue() {
    assertEquals(TypeIncident.VOL.jsonValue(), mapper.extractIncidentTypeValue(TypeIncident.VOL));
    assertNull(mapper.extractIncidentTypeValue(null));
  }

  @Test
  void shouldBuildProcessDataForCyberCommandeFrauduleuse() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.COMMANDE_FRAUDULEUSE);

    ProcessData result = mapper.buildProcessData(cybercrime, "SRC");

    assertEquals(Ech051Constants.SourceIds.CYBERCRIME_COMMANDE_FRAUDULEUSE, result.getSourceId());
  }

  @Test
  void shouldBuildProcessDataForCyberFausseAnnonce() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.FAUSSE_ANNONCE);

    ProcessData result = mapper.buildProcessData(cybercrime, "SRC");

    assertEquals(Ech051Constants.SourceIds.CYBERCRIME_FAUSSE_ANNONCE, result.getSourceId());
  }

  @Test
  void shouldBuildProcessDataForGenericCybercrime() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.RANCONGICIEL);

    ProcessData result = mapper.buildProcessData(cybercrime, "SRC");

    assertEquals(Ech051Constants.SourceIds.CYBERCRIME, result.getSourceId());
  }

  @Test
  void shouldResolveCyberActionPeriodFromCommandeDateDecouverte() {
    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setDateDecouverte("2026-02-15");

    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    when(cybercrime.getCommandeFrauduleuse()).thenReturn(cf);
    when(cybercrime.getAchatNonRecu()).thenReturn(null);
    when(cybercrime.getFausseAnnonce()).thenReturn(null);
    when(cybercrime.getDateDebutEvent()).thenReturn("2026-01-01");
    when(cybercrime.getDateFinEvent()).thenReturn("2026-01-02");
    when(cybercrime.getDescriptionCybercrime()).thenReturn("X");
    when(cybercrime.getTypeLieu()).thenReturn(null);

    Event event = mapper.buildEvent(cybercrime, null);

    assertEquals("2026-02-15", event.getActionPeriod().getFrom());
    assertEquals("2026-02-15", event.getActionPeriod().getTo());
  }

  @Test
  void shouldResolveCyberActionPeriodAchatWhenOnlyPremierContact() {
    AchatNonRecu achat = new AchatNonRecu();

    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.ACHAT_NON_RECU);
    when(cybercrime.getAchatNonRecu()).thenReturn(achat);
    when(cybercrime.getCommandeFrauduleuse()).thenReturn(null);
    when(cybercrime.getFausseAnnonce()).thenReturn(null);
    when(cybercrime.getDatePremierContact()).thenReturn("2026-03-01T10:00:00+01:00");
    when(cybercrime.getDescriptionCybercrime()).thenReturn("D");
    when(cybercrime.getTypeLieu()).thenReturn(null);

    Event event = mapper.buildEvent(cybercrime, null);

    assertEquals("2026-03-01T10:00:00+01:00", event.getActionPeriod().getFrom());
    assertEquals("2026-03-01T10:00:00+01:00", event.getActionPeriod().getTo());
  }

  @Test
  void shouldBuildModeOperandiAndTypeOfCrimeForCyberCommande() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.COMMANDE_FRAUDULEUSE);

    RipolReference mode = mapper.buildModeOperandiReference(cybercrime);
    RipolReference crime = mapper.buildTypeOfCrimeReference(cybercrime);

    assertNotNull(mode);
    assertEquals(Ech051Constants.MODE_OPERANDI_CYBER_COMMANDE_FRAUDULEUSE_CODE, mode.getCode());
    assertNotNull(crime);
    assertEquals(Ech051Constants.TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_CODE, crime.getCode());
  }

  @Test
  void shouldBuildModeOperandiAndTypeOfCrimeForCyberFausseAnnonce() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.FAUSSE_ANNONCE);

    RipolReference mode = mapper.buildModeOperandiReference(cybercrime);
    RipolReference crime = mapper.buildTypeOfCrimeReference(cybercrime);

    assertNotNull(mode);
    assertEquals(Ech051Constants.MODE_OPERANDI_CYBER_FAUSSE_ANNONCE_CODE, mode.getCode());
    assertNotNull(crime);
    assertEquals(Ech051Constants.TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_CODE, crime.getCode());
  }

  @Test
  void shouldIncludeFactsOnCyberTransactionEvent() {
    Cybercrime cybercrime = mock(Cybercrime.class);
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.ACHAT_NON_RECU);
    when(cybercrime.getAchatNonRecu()).thenReturn(new AchatNonRecu());
    when(cybercrime.getCommandeFrauduleuse()).thenReturn(null);
    when(cybercrime.getFausseAnnonce()).thenReturn(null);
    when(cybercrime.getDateDebutEvent()).thenReturn("2026-01-01");
    when(cybercrime.getDateFinEvent()).thenReturn("2026-01-02");
    when(cybercrime.getDescriptionCybercrime()).thenReturn("Faits synthétiques");
    when(cybercrime.getTypeLieu()).thenReturn(null);

    Event event = mapper.buildEvent(cybercrime, null);

    assertEquals("Faits synthétiques", event.getFacts());
  }

  @Test
  void shouldBuildEventAdditionalInformationForAchatNonRecu() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setMontantDelitAchatLigne("100");
    achat.setEmailVendeurInconnu(true);
    achat.setAchatViaPlaceMarche(false);

    Cybercrime cybercrime = mock(Cybercrime.class);
    cybercrime.setDatePremierContact("2026-01-01");
    when(cybercrime.getTypeCybercrime()).thenReturn(TypeCybercrime.ACHAT_NON_RECU);
    when(cybercrime.getAchatNonRecu()).thenReturn(achat);

    String info = mapper.buildEventAdditionalInformation(cybercrime);

    assertNotNull(info);
    assertTrue(info.contains("Montant du delit"));
    assertTrue(info.contains("oui"));
  }
}
