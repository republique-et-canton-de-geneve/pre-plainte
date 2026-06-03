package ch.ge.police.infrastructure.ech051;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentXml;
import ch.ge.police.infrastructure.ech051.dto.Ech0058MessageHeader;
import ch.ge.police.infrastructure.ech051.dto.Ech0058MessageHeaderXml;
import ch.ge.police.infrastructure.ech051.dto.SepContentWrapperXml;
import ch.ge.police.infrastructure.ech051.dto.SepDeliveryEnvelopeXml;
import ch.ge.police.infrastructure.ech051.dto.SepDeliveryPayload;
import ch.ge.police.infrastructure.ech051.mapper.SuisseEpoliceMapperForPPL;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.EMAIL;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.EMAIL_CODE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.MOBILE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.MOBILE_CODE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.PHONE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.PHONE_CODE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.CommunicationUsageLabels.URI;

/**
 * Assemble l'enveloppe SEP complète à partir des DTO "core" et la sérialise en XML
 * conforme aux schémas eCH-0051/eCH-0058 grâce aux classes JAXB du package dto.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Ech051Builder {

  private static final String RIPOL_SOURCE = "RIPOL";
  private static final DateTimeFormatter SEP_LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private final SuisseEpoliceMapperForPPL mapper;

  private static final JAXBContext SEP_DELIVERY_ENVELOPE_CONTEXT = initJaxbContext();

  @Value("${ech051.sender-id:T4-489775-8}")
  private String senderId;

  @Value("${ech051.recipient-id:PPL}")
  private String recipientId;

  @Value("${ech051.message-type:5350}")
  private String defaultMessageType;

  @Value("${ech051.action:1}")
  private String action;

  @Value("${ech051.sending-application.manufacturer:Bedag Informatik AG}")
  private String manufacturer;

  @Value("${ech051.sending-application.product:Suisse ePolice Deux}")
  private String product;

  @Value("${ech051.sending-application.version:1.0}")
  private String productVersion;

  private static JAXBContext initJaxbContext() {
    try {
      return JAXBContext.newInstance(SepDeliveryEnvelopeXml.class);
    } catch (JAXBException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public String generateEch051Xml(PrePlainte prePlainte, boolean isTestMode) {
    Ech0051DocumentPayload documentDto = mapper.toDocument(prePlainte);
    SepDeliveryPayload delivery = SepDeliveryPayload.builder().header(buildHeaderDto(documentDto, isTestMode, prePlainte.getDemandeId())).document(documentDto).build();

    try {
      SepDeliveryEnvelopeXml xml = mapToXml(delivery);
      Marshaller marshaller = SEP_DELIVERY_ENVELOPE_CONTEXT.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      marshaller.marshal(xml, baos);
      return baos.toString(java.nio.charset.StandardCharsets.UTF_8);
    } catch (JAXBException e) {
      throw new IllegalStateException("Erreur lors de la génération du XML eCH-0051", e);
    }
  }

  private Ech0058MessageHeader buildHeaderDto(Ech0051DocumentPayload documentDto, boolean isTestMode, String demandeId) {
    String messageDate = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String resolvedMessageType = resolveMessageType(documentDto);
    return Ech0058MessageHeader.builder().senderId(senderId).recipientId(recipientId).messageId(demandeId).messageType(resolvedMessageType).sendingApplication(Ech0058MessageHeader.SendingApplication.builder().manufacturer(manufacturer).product(product).productVersion(productVersion).build()).messageDate(messageDate).eventDate(Optional.ofNullable(documentDto.getEvents()).flatMap(events -> events.stream().findFirst()).map(Ech0051DocumentPayload.Event::getComplaintDate).orElse(messageDate)).action(action).testDeliveryFlag(isTestMode).build();
  }

  private SepDeliveryEnvelopeXml mapToXml(SepDeliveryPayload deliveryDto) {
    SepDeliveryEnvelopeXml deliveryXml = new SepDeliveryEnvelopeXml();
    deliveryXml.setHeader(mapHeader(deliveryDto.getHeader()));
    SepContentWrapperXml contentXml = new SepContentWrapperXml();
    contentXml.setDocument(mapDocument(deliveryDto.getDocument()));
    deliveryXml.setContent(contentXml);
    return deliveryXml;
  }

  private Ech0058MessageHeaderXml mapHeader(Ech0058MessageHeader headerDto) {
    Ech0058MessageHeaderXml headerXml = new Ech0058MessageHeaderXml();
    headerXml.setSenderId(headerDto.getSenderId());
    headerXml.setRecipientId(headerDto.getRecipientId());
    headerXml.setMessageId(headerDto.getMessageId());
    headerXml.setMessageType(headerDto.getMessageType());
    headerXml.setMessageDate(headerDto.getMessageDate());
    headerXml.setEventDate(headerDto.getEventDate());
    headerXml.setAction(headerDto.getAction());
    headerXml.setTestDeliveryFlag(headerDto.isTestDeliveryFlag());

    Ech0058MessageHeaderXml.SendingApplicationXml sendingApplicationXml = new Ech0058MessageHeaderXml.SendingApplicationXml();
    sendingApplicationXml.setManufacturer(headerDto.getSendingApplication().getManufacturer());
    sendingApplicationXml.setProduct(headerDto.getSendingApplication().getProduct());
    sendingApplicationXml.setProductVersion(headerDto.getSendingApplication().getProductVersion());
    headerXml.setSendingApplication(sendingApplicationXml);
    return headerXml;
  }

  private Ech0051DocumentXml mapDocument(Ech0051DocumentPayload documentDto) {
    Ech0051DocumentXml documentXml = new Ech0051DocumentXml();
    documentXml.setXmlLang(resolveDocumentLanguage(documentDto));
    documentXml.setProcessData(mapProcessData(documentDto.getProcessData()));

    if (documentDto.getPersons() != null) {
      documentDto.getPersons().forEach(person -> documentXml.getPersons().add(mapPerson(person)));
    }
    if (documentDto.getVehicles() != null) {
      documentDto.getVehicles().forEach(vehicle -> documentXml.getVehicles().add(mapVehicle(vehicle)));
    }
    if (documentDto.getObjects() != null) {
      documentDto.getObjects().forEach(object -> documentXml.getObjects().add(mapObject(object)));
    }
    if (documentDto.getEvents() != null) {
      documentDto.getEvents().forEach(event -> documentXml.getEvents().add(mapEvent(event)));
    }
    if (documentDto.getBusinessCases() != null) {
      documentDto.getBusinessCases().forEach(businessCase -> documentXml.getBusinessCases().add(mapBusinessCase(businessCase)));
    }
    Ech0051DocumentXml.RelationsXml relationsXml = mapRelations(documentDto.getRelations());
    documentXml.setRelations(relationsXml);
    return documentXml;
  }

  private Ech0051DocumentXml.ProcessDataXml mapProcessData(Ech0051DocumentPayload.ProcessData processData) {
    if (processData == null) {
      return null;
    }
    Ech0051DocumentXml.ProcessDataXml xml = new Ech0051DocumentXml.ProcessDataXml();
    xml.setDeliveryDate(processData.getDeliveryDate());

    Ech0051DocumentXml.SourceIdXml sourceIdXml = new Ech0051DocumentXml.SourceIdXml();
    sourceIdXml.setSource("SEP");
    sourceIdXml.setSourceTable(processData.getSourceId());
    sourceIdXml.setValue(processData.getSourceValue() != null ? processData.getSourceValue() : "PPL");
    xml.setSourceId(sourceIdXml);

    if (processData.getProcessingStatus() != null) {
      Ech0051DocumentXml.ProcessingStatusXml statusXml = new Ech0051DocumentXml.ProcessingStatusXml();
      Ech0051DocumentXml.SourceIdXml statusSourceId = new Ech0051DocumentXml.SourceIdXml();
      statusSourceId.setSource("SEP");
      statusSourceId.setSourceTable("SIGNAL_COLOR");
      statusSourceId.setValue(processData.getProcessingStatus());
      statusXml.setSourceId(statusSourceId);
      xml.setProcessingStatus(statusXml);
    }
    return xml;
  }

  private Ech0051DocumentXml.PersonXml mapPerson(Ech0051DocumentPayload.Person personDto) {
    Ech0051DocumentXml.PersonXml personXml = new Ech0051DocumentXml.PersonXml();
    personXml.setKey(personDto.getKey());

    if (personDto.getNaturalIdentity() != null) {
      Ech0051DocumentXml.NaturalXml naturalXml = new Ech0051DocumentXml.NaturalXml();
      naturalXml.setIdentity(mapNaturalIdentity(personDto.getNaturalIdentity()));
      personXml.setNatural(naturalXml);
    }
    if (personDto.getLegalIdentity() != null) {
      Ech0051DocumentXml.LegalXml legalXml = new Ech0051DocumentXml.LegalXml();
      legalXml.setCurrentName(personDto.getLegalIdentity().getCurrentName());
      personXml.setLegal(legalXml);
    }
    if (personDto.getAddress() != null) {
      personXml.setAddress(mapAddress(personDto.getAddress()));
    }
    if (personDto.getCommunication() != null) {
      appendMeansOfCommunicationFromDto(personXml, personDto.getCommunication());
    }
    personXml.setDeliveredAbroad(personDto.getDeliveredAbroad() != null ? personDto.getDeliveredAbroad().toString() : null);
    personXml.setAdditionalInformation(personDto.getAdditionalInformation());
    return personXml;
  }

  private Ech0051DocumentXml.NaturalIdentityXml mapNaturalIdentity(Ech0051DocumentPayload.NaturalIdentity dto) {
    Ech0051DocumentXml.NaturalIdentityXml identityXml = new Ech0051DocumentXml.NaturalIdentityXml();
    identityXml.setKey(dto.getKey());
    identityXml.setIdentityCategory(mapIdentityCategory(dto.getIdentityCategory()));
    identityXml.setAdditionalInformation(dto.getAdditionalInformation());
    identityXml.setOfficialName(dto.getOfficialName());
    identityXml.setOriginalName(dto.getOriginalName());
    identityXml.setFirstName(dto.getFirstName());
    identityXml.setCallName(dto.getCallName());
    identityXml.setSex(mapSex(dto.getSex()));

    if (dto.getBirthDate() != null && !dto.getBirthDate().isBlank()) {
      Ech0051DocumentXml.BirthDataXml birth = new Ech0051DocumentXml.BirthDataXml();
      birth.setDate(dto.getBirthDate().strip());
      identityXml.setBirthData(birth);
    }

    if (dto.getLanguageCode() != null && !dto.getLanguageCode().isBlank()) {
      Ech0051DocumentXml.LanguageXml languageXml = new Ech0051DocumentXml.LanguageXml();
      languageXml.setLanguageIsoCode(dto.getLanguageCode().strip());
      identityXml.setLanguage(languageXml);
    }

    if (dto.getPlaceOfOrigin() != null) {
      identityXml.setPlaceOfOrigin(mapPlaceOfOrigin(dto.getPlaceOfOrigin()));
    }

    identityXml.setSocialSecurityNumber(dto.getSocialSecurityNumber());
    identityXml.setNationality(mapNationality(dto.getNationality()));
    identityXml.setProfession(dto.getProfession());
    return identityXml;
  }

  private Ech0051DocumentXml.IdentityCategoryXml mapIdentityCategory(String identityCategory) {
    if (identityCategory == null || identityCategory.isBlank()) {
      return null;
    }
    Ech0051DocumentXml.IdentityCategoryXml identityCategoryXml = new Ech0051DocumentXml.IdentityCategoryXml();
    identityCategoryXml.setMarking(identityCategory);
    return identityCategoryXml;
  }

  private Ech0051DocumentXml.PlaceOfOriginXml mapPlaceOfOrigin(Ech0051DocumentPayload.RipolLocation dto) {
    Ech0051DocumentXml.PlaceOfOriginXml placeXml = new Ech0051DocumentXml.PlaceOfOriginXml();
    placeXml.setMarking(buildMarkingWithLang(dto.getLabel()));
    Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
    sourceId.setSource(RIPOL_SOURCE);
    sourceId.setSourceTable(dto.getSourceTable());
    sourceId.setValue(dto.getCode());
    placeXml.setSourceId(sourceId);
    placeXml.setZipCode(dto.getZipCode());
    return placeXml;
  }

  private Ech0051DocumentXml.SexXml mapSex(Ech0051DocumentPayload.RipolReference ref) {
    if (ref == null || ref.getCode() == null) {
      return null;
    }
    Ech0051DocumentXml.SexXml sexXml = new Ech0051DocumentXml.SexXml();
    sexXml.setMarking(buildMarkingWithLang(ref.getLabel()));
    sexXml.setSourceId(buildSourceId(ref));
    return sexXml;
  }

  private Ech0051DocumentXml.NationalityXml mapNationality(Ech0051DocumentPayload.RipolReference ref) {
    if (ref == null || ref.getCode() == null) {
      return null;
    }
    Ech0051DocumentXml.NationalityXml nationalityXml = new Ech0051DocumentXml.NationalityXml();
    nationalityXml.setMarking(buildMarkingWithLang(ref.getLabel()));
    nationalityXml.setSourceId(buildSourceId(ref));
    return nationalityXml;
  }

  private Ech0051DocumentXml.RipolValueXml mapRipolValue(Ech0051DocumentPayload.RipolReference ref) {
    if (ref == null || ref.getCode() == null) {
      return null;
    }
    Ech0051DocumentXml.RipolValueXml ripolXml = new Ech0051DocumentXml.RipolValueXml();
    ripolXml.setMarking(buildMarkingWithLang(ref.getLabel()));
    ripolXml.setSourceId(buildSourceId(ref));
    return ripolXml;
  }

  private Ech0051DocumentXml.MarkingWithLangXml buildMarkingWithLang(String value) {
    if (value == null) {
      return null;
    }
    Ech0051DocumentXml.MarkingWithLangXml marking = new Ech0051DocumentXml.MarkingWithLangXml();
    marking.setLang("fr");
    marking.setValue(value);
    return marking;
  }

  private Ech0051DocumentXml.SourceIdXml buildSourceId(Ech0051DocumentPayload.RipolReference ref) {
    Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
    sourceId.setSource(ref.getSource());
    sourceId.setSourceTable(ref.getSourceTable());
    sourceId.setValue(ref.getCode());
    return sourceId;
  }

  private Ech0051DocumentXml.AddressXml mapAddress(Ech0051DocumentPayload.Address dto) {
    Ech0051DocumentXml.AddressXml addressXml = new Ech0051DocumentXml.AddressXml();
    String streetLine = dto.getStreet();
    if (streetLine == null || streetLine.isBlank()) {
      streetLine = dto.getAdditional();
    }
    StreetParts streetParts = splitStreetAndNumber(streetLine);
    addressXml.setStreet(buildMarkedValue(streetParts.street()));
    addressXml.setHouseNumber(dto.getHouseNumber() != null ? dto.getHouseNumber() : streetParts.houseNumber());
    if (dto.getPlace() != null) {
      addressXml.setPlace(mapAddressPlace(dto.getPlace()));
    }
    if (dto.getCountry() != null) {
      addressXml.setCountry(mapCountry(dto.getCountry()));
    }
    return addressXml;
  }

  private Ech0051DocumentXml.AddressPlaceXml mapAddressPlace(Ech0051DocumentPayload.RipolLocation dto) {
    if (dto == null) {
      return null;
    }
    Ech0051DocumentXml.AddressPlaceXml placeXml = new Ech0051DocumentXml.AddressPlaceXml();
    placeXml.setMarking(buildMarkingWithLang(dto.getLabel()));
    if (dto.getCode() != null) {
      Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
      sourceId.setSource(RIPOL_SOURCE);
      sourceId.setSourceTable(dto.getSourceTable());
      sourceId.setValue(dto.getCode());
      placeXml.setSourceId(sourceId);
    }
    placeXml.setZipCode(dto.getZipCode());
    return placeXml;
  }

  private Ech0051DocumentXml.CountryXml mapCountry(Ech0051DocumentPayload.RipolLocation dto) {
    if (dto == null) {
      return null;
    }
    Ech0051DocumentXml.CountryXml countryXml = new Ech0051DocumentXml.CountryXml();
    countryXml.setMarking(buildMarkingWithLang(dto.getLabel()));
    if (dto.getCode() != null) {
      Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
      sourceId.setSource(RIPOL_SOURCE);
      sourceId.setSourceTable(dto.getSourceTable());
      sourceId.setValue(dto.getCode());
      countryXml.setSourceId(sourceId);
    }
    return countryXml;
  }

  /**
   * Un bloc {@code meansOfCommunication} par canal renseigné (aligné sur les exemples eCH-0051 / SEP).
   */
  private void appendMeansOfCommunicationFromDto(
      Ech0051DocumentXml.PersonXml personXml,
      Ech0051DocumentPayload.Communication dto
  ) {
    if (dto == null || personXml == null) {
      return;
    }
    if (isNotBlank(dto.getEmail())) {
      Ech0051DocumentXml.CommunicationXml commXml = new Ech0051DocumentXml.CommunicationXml();
      Ech0051DocumentXml.EmailXml emailXml = new Ech0051DocumentXml.EmailXml();
      emailXml.setUsage(buildUsage(EMAIL, EMAIL_CODE));
      emailXml.setEmailAddress(dto.getEmail().strip());
      commXml.setEmail(emailXml);
      personXml.getCommunications().add(commXml);
    }
    if (isNotBlank(dto.getMobile())) {
      Ech0051DocumentXml.CommunicationXml commXml = new Ech0051DocumentXml.CommunicationXml();
      commXml.setMobile(buildPhone(dto.getMobile().strip(), MOBILE, MOBILE_CODE));
      personXml.getCommunications().add(commXml);
    }
    if (isNotBlank(dto.getPhone())) {
      Ech0051DocumentXml.CommunicationXml commXml = new Ech0051DocumentXml.CommunicationXml();
      commXml.setPhone(buildPhone(dto.getPhone().strip(), PHONE, PHONE_CODE));
      personXml.getCommunications().add(commXml);
    }
    if (isNotBlank(dto.getUri())) {
      Ech0051DocumentXml.CommunicationXml commXml = new Ech0051DocumentXml.CommunicationXml();
      Ech0051DocumentXml.UriXml uriXml = new Ech0051DocumentXml.UriXml();
      uriXml.setUsage(buildUsageWithoutSource(URI));
      uriXml.setUri(dto.getUri().strip());
      commXml.setUri(uriXml);
      personXml.getCommunications().add(commXml);
    }
  }

  private static boolean isNotBlank(String s) {
    return s != null && !s.isBlank();
  }

  private Ech0051DocumentXml.EventXml mapEvent(Ech0051DocumentPayload.Event dto) {
    Ech0051DocumentXml.EventXml eventXml = new Ech0051DocumentXml.EventXml();
    eventXml.setKey(dto.getKey());
    eventXml.setDescriptionShort(dto.getDescriptionShort());
    eventXml.setComplaintDate(dto.getComplaintDate());

    if (dto.getActionPlace() != null || dto.getActionPeriod() != null) {
      eventXml.getActionPlaceGroups().add(
        mapActionPlaceGroup("1", dto.getActionPlace(), dto.getActionPeriod())
      );
    }

    if (dto.getSecondaryActionPlace() != null) {
      eventXml.getActionPlaceGroups().add(
        mapActionPlaceGroup("2", dto.getSecondaryActionPlace(), dto.getActionPeriod())
      );
    }

    if (dto.getBootyAmount() != null) {
      Ech0051DocumentXml.BootyAmountXml bootyXml = new Ech0051DocumentXml.BootyAmountXml();
      bootyXml.setAmount(dto.getBootyAmount());
      eventXml.setBootyAmount(bootyXml);
    }

    if (dto.getLocality() != null) {
      Ech0051DocumentXml.LocalityXml localityXml = new Ech0051DocumentXml.LocalityXml();
      localityXml.setLocalityCode(mapRipolValue(dto.getLocality()));
      eventXml.setLocality(localityXml);
    }

    eventXml.setModeOperandi(mapRipolValue(dto.getModeOperandi()));
    eventXml.setTypeOfCrime(mapRipolValue(dto.getTypeOfCrime()));
    eventXml.setFacts(dto.getFacts());
    eventXml.setAdditionalInformation(dto.getAdditionalInformation());
    return eventXml;
  }

  private Ech0051DocumentXml.ActionPlaceGroupXml mapActionPlaceGroup(
    String mainRecord,
    Ech0051DocumentPayload.ActionPlace placeDto,
    Ech0051DocumentPayload.ActionPeriod periodDto
  ) {
    Ech0051DocumentXml.ActionPlaceGroupXml groupXml = new Ech0051DocumentXml.ActionPlaceGroupXml();
    groupXml.setMainRecord(mainRecord);

    if (placeDto != null) {
      Ech0051DocumentXml.ActionPlaceXml placeXml = new Ech0051DocumentXml.ActionPlaceXml();
      StreetParts streetParts = splitStreetAndNumber(placeDto.getStreet());
      placeXml.setStreet(buildMarkedValue(streetParts.street()));
      placeXml.setHouseNumber(placeDto.getHouseNumber() != null ? placeDto.getHouseNumber() : streetParts.houseNumber());
      if (placeDto.getPlace() != null) {
        placeXml.setPlace(mapAddressPlace(placeDto.getPlace()));
      }
      if (placeDto.getCityArea() != null && !placeDto.getCityArea().isBlank()) {
        placeXml.setCityArea(buildMarkedValue(placeDto.getCityArea().strip()));
      }
      if (placeDto.getCountry() != null) {
        placeXml.setCountry(mapCountry(placeDto.getCountry()));
      }
      groupXml.setActionPlace(placeXml);
    }

    if (periodDto != null) {
      groupXml.setActionPeriodFrom(formatActionPeriodDate(periodDto.getFrom()));
      groupXml.setActionPeriodTo(formatActionPeriodDate(periodDto.getTo()));
    }

    return groupXml;
  }

  private String formatActionPeriodDate(String dateTime) {
    if (dateTime == null || dateTime.isBlank()) {
      return null;
    }
    String value = dateTime.strip();
    try {
      if (value.contains("T")) {
        return parseActionPeriodDateTime(value).format(SEP_LOCAL_DATE_TIME_FORMATTER);
      }
      return LocalDate.parse(value).atStartOfDay().format(SEP_LOCAL_DATE_TIME_FORMATTER);
    } catch (Exception e) {
      return dateTime;
    }
  }

  private LocalDateTime parseActionPeriodDateTime(String value) {
    try {
      return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
    } catch (DateTimeParseException ignored) {
      return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    }
  }

  private Ech0051DocumentXml.ObjectXml mapObject(Ech0051DocumentPayload.ObjectItem dto) {
    Ech0051DocumentXml.ObjectXml objectXml = new Ech0051DocumentXml.ObjectXml();
    objectXml.setKey(dto.getKey());
    objectXml.setAdditionalInformation(dto.getAdditionalInformation());

    Ech0051DocumentXml.ObjectDefinitionXml definitionXml = new Ech0051DocumentXml.ObjectDefinitionXml();
    definitionXml.setGravure(dto.getGravure());
    definitionXml.setMarkOther(dto.getFabricantAutre());
    definitionXml.setModelOther(dto.getModeleAutre());
    definitionXml.setTypeOfObject(mapRipolValue(dto.getTypeOfObject()));
    definitionXml.setMark(mapRipolValue(dto.getFabricant()));
    definitionXml.setModelType(mapRipolValue(dto.getModele()));
    if (dto.getMaterial() != null) {
      definitionXml.setMaterial(buildMarkedValue(dto.getMaterial()));
    }
    definitionXml.setColour(mapRipolValue(dto.getCouleur()));
    definitionXml.setColourSecondary(mapRipolValue(dto.getCouleurSecondaire()));
    if (dto.getRealValue() != null) {
      Ech0051DocumentXml.RealValueXml realValueXml = new Ech0051DocumentXml.RealValueXml();
      realValueXml.setAmount(dto.getRealValue());
      definitionXml.setRealValue(realValueXml);
    }
    definitionXml.setPurchaseDate(dto.getPurchaseDate());
    objectXml.setDefinition(definitionXml);

    if (dto.getIdentification() != null) {
      Ech0051DocumentXml.IdentificationXml identificationXml = new Ech0051DocumentXml.IdentificationXml();
      identificationXml.setType(dto.getIdentification().getType());
      identificationXml.setIsUnique("1");
      identificationXml.setNumber(dto.getIdentification().getNumber());
      objectXml.setIdentificationNumber(identificationXml);
    }
    return objectXml;
  }

  private Ech0051DocumentXml.VehicleXml mapVehicle(Ech0051DocumentPayload.VehicleItem dto) {
    Ech0051DocumentXml.VehicleXml vehicleXml = new Ech0051DocumentXml.VehicleXml();
    vehicleXml.setKey(dto.getKey());
    vehicleXml.setAdditionalInformation(dto.getAdditionalInformation());

    Ech0051DocumentXml.VehicleDefinitionXml definitionXml = new Ech0051DocumentXml.VehicleDefinitionXml();
    definitionXml.setVelofinderId(dto.getVelofinderId());
    definitionXml.setPurchaseDate(dto.getPurchaseDate());
    definitionXml.setVignetteNumber(dto.getVignetteNumber());
    definitionXml.setMasterNumber(dto.getMasterNumber());
    definitionXml.setFrameNumber(dto.getFrameNumber());
    definitionXml.setMarkOther(dto.getMarkOther());
    definitionXml.setModelOther(dto.getModelOther());
    definitionXml.setVin(dto.getVin());
    if (dto.getTypeOfVehicle() != null) {
      Ech0051DocumentXml.TypeOfVehicleGroupXml typeGroup = new Ech0051DocumentXml.TypeOfVehicleGroupXml();
      typeGroup.setTypeOfVehicle(mapRipolValue(dto.getTypeOfVehicle()));
      definitionXml.setTypeOfVehicleGroup(typeGroup);
    }
    if (dto.getMark() != null) {
      definitionXml.setMark(mapRipolValue(dto.getMark()));
    }
    if (dto.getModelType() != null) {
      definitionXml.setModelType(mapRipolValue(dto.getModelType()));
    }
    if (dto.getColour() != null) {
      definitionXml.setColour(mapRipolValue(dto.getColour()));
    }
    if (dto.getColourSecondary() != null) {
      definitionXml.setColourSecondary(mapRipolValue(dto.getColourSecondary()));
    }
    vehicleXml.setDefinition(definitionXml);

    if (dto.getNumberPlate() != null) {
      vehicleXml.setNumberPlate(mapNumberPlate(dto.getNumberPlate()));
    }
    return vehicleXml;
  }

  private Ech0051DocumentXml.NumberPlateXml mapNumberPlate(Ech0051DocumentPayload.NumberPlate dto) {
    if (dto == null) {
      return null;
    }
    Ech0051DocumentXml.NumberPlateXml plateXml = new Ech0051DocumentXml.NumberPlateXml();
    plateXml.setNumber(dto.getNumber());
    if (dto.getCountry() != null) {
      plateXml.setCountry(mapCountry(dto.getCountry()));
    }
    if (dto.getCanton() != null) {
      plateXml.setCanton(mapCanton(dto.getCanton()));
    }
    return plateXml;
  }

  private Ech0051DocumentXml.CantonXml mapCanton(Ech0051DocumentPayload.RipolLocation dto) {
    if (dto == null) {
      return null;
    }
    Ech0051DocumentXml.CantonXml cantonXml = new Ech0051DocumentXml.CantonXml();
    cantonXml.setMarking(buildMarkingWithLang(dto.getLabel()));
    if (dto.getCode() != null) {
      Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
      sourceId.setSource(RIPOL_SOURCE);
      sourceId.setSourceTable(dto.getSourceTable());
      sourceId.setValue(dto.getCode());
      cantonXml.setSourceId(sourceId);
    }
    return cantonXml;
  }

  private Ech0051DocumentXml.BusinessCaseXml mapBusinessCase(Ech0051DocumentPayload.BusinessCase dto) {
    Ech0051DocumentXml.BusinessCaseXml businessCaseXml = new Ech0051DocumentXml.BusinessCaseXml();
    businessCaseXml.setKey(dto.getKey());
    businessCaseXml.setCaseNumber(dto.getCaseNumber());

    if (dto.getFile() != null) {
      List<Ech0051DocumentXml.FileXml> files = dto.getFile().stream().map(this::mapFile).toList();
      businessCaseXml.setFile(files);
    }

    return businessCaseXml;
  }

  private Ech0051DocumentXml.FileXml mapFile(Ech0051DocumentPayload.File dto) {
    Ech0051DocumentXml.FileXml fileXml = new Ech0051DocumentXml.FileXml();

    if (dto.getAttachment() != null) {
      List<Ech0051DocumentXml.AttachmentXml> attachments = dto.getAttachment().stream().map(this::mapAttachment).toList();
      fileXml.setAttachment(attachments);
    }

    return fileXml;
  }

  private Ech0051DocumentXml.AttachmentXml mapAttachment(Ech0051DocumentPayload.Attachment dto) {
    Ech0051DocumentXml.AttachmentXml attachmentXml = new Ech0051DocumentXml.AttachmentXml();
    attachmentXml.setFilename(dto.getFilename());
    attachmentXml.setContent(dto.getContent());
    return attachmentXml;
  }


  private Ech0051DocumentXml.RelationsXml mapRelations(Ech0051DocumentPayload.Relations dto) {
    Ech0051DocumentXml.RelationsXml relationsXml = new Ech0051DocumentXml.RelationsXml();
    if (dto == null) {
      return relationsXml;
    }
    if (dto.getPersonLinks() != null) {
      dto.getPersonLinks().forEach(link -> {
        Ech0051DocumentXml.PersonLinkXml personLink = new Ech0051DocumentXml.PersonLinkXml();
        personLink.setPerson1Role(mapRipolValue(link.getPerson1Role()));
        personLink.setPerson1Ref(link.getPerson1Ref());
        personLink.setPerson2Role(mapRipolValue(link.getPerson2Role()));
        personLink.setPerson2Ref(link.getPerson2Ref());
        relationsXml.getPersonLinks().add(personLink);
      });
    }
    if (dto.getFinancialTransactions() != null) {
      dto.getFinancialTransactions().forEach(tx -> addFinancialTransactionXml(relationsXml, tx));
    }
    if (dto.getInvolvedParties() != null) {
      dto.getInvolvedParties().forEach(rel -> {
        Ech0051DocumentXml.InvolvedPartyXml involved = new Ech0051DocumentXml.InvolvedPartyXml();
        involved.setBusinessCaseRef(rel.getBusinessCaseRef());
        involved.setTypeOfInvolvement(mapRipolValue(rel.getTypeOfInvolvement()));
        involved.setPersonRef(rel.getPersonRef());
        involved.setEventRef(rel.getEventRef());
        relationsXml.getInvolvedParties().add(involved);
      });
    }
    if (dto.getEventBusinessCaseLinks() != null) {
      dto.getEventBusinessCaseLinks().forEach(link -> {
        Ech0051DocumentXml.EventBusinessCaseLinkXml ebcLink = new Ech0051DocumentXml.EventBusinessCaseLinkXml();
        ebcLink.setEventRef(link.getEventRef());
        ebcLink.setBusinessCaseRef(link.getBusinessCaseRef());
        relationsXml.getEventBusinessCaseLinks().add(ebcLink);
      });
    }
    if (dto.getEventObjectLinks() != null) {
      dto.getEventObjectLinks().forEach(link -> {
        Ech0051DocumentXml.EventObjectLinkXml eoLink = new Ech0051DocumentXml.EventObjectLinkXml();
        eoLink.setEventRef(link.getEventRef());
        eoLink.setObjectRef(link.getObjectRef());
        eoLink.setObjectRole(mapRipolValue(link.getObjectRole()));
        relationsXml.getEventObjectLinks().add(eoLink);
      });
    }
    if (dto.getEventVehicleLinks() != null) {
      dto.getEventVehicleLinks().forEach(link -> {
        Ech0051DocumentXml.EventVehicleLinkXml evLink = new Ech0051DocumentXml.EventVehicleLinkXml();
        evLink.setEventRef(link.getEventRef());
        evLink.setVehicleRef(link.getVehicleRef());
        evLink.setVehicleRole(mapRipolValue(link.getVehicleRole()));
        relationsXml.getEventVehicleLinks().add(evLink);
      });
    }
    if (dto.getObjectPersonLinks() != null) {
      dto.getObjectPersonLinks().forEach(link -> {
        Ech0051DocumentXml.ObjectPersonLinkXml objectLink = new Ech0051DocumentXml.ObjectPersonLinkXml();
        objectLink.setObjectRef(link.getObjectRef());
        objectLink.setPersonRef(link.getPersonRef());
        objectLink.setInsurerRef(link.getInsurerRef());
        objectLink.setPersonRole(mapRipolValue(link.getPersonRole()));
        relationsXml.getObjectPersonLinks().add(objectLink);
      });
    }
    if (dto.getVehiclePersonLinks() != null) {
      dto.getVehiclePersonLinks().forEach(link -> {
        Ech0051DocumentXml.VehiclePersonLinkXml vpLink = new Ech0051DocumentXml.VehiclePersonLinkXml();
        vpLink.setVehicleRef(link.getVehicleRef());
        vpLink.setPersonRef(link.getPersonRef());
        vpLink.setInsurerRef(link.getInsurerRef());
        vpLink.setInsuranceNumber(link.getInsuranceNumber());
        vpLink.setPersonRole(mapRipolValue(link.getPersonRole()));
        relationsXml.getVehiclePersonLinks().add(vpLink);
      });
    }
    return relationsXml;
  }

  private void addFinancialTransactionXml(Ech0051DocumentXml.RelationsXml relationsXml, Ech0051DocumentPayload.FinancialTransaction tx) {
    Ech0051DocumentXml.FinancialTransactionXml txXml = new Ech0051DocumentXml.FinancialTransactionXml();
    txXml.setPaymentType(tx.getPaymentType());
    txXml.setTransactionNumber(tx.getTransactionNumber());
    txXml.setPlatformType(tx.getPlatformType());
    txXml.setPlatformId(tx.getPlatformId());
    txXml.setPaymentDateTime(tx.getPaymentDateTime());
    txXml.setPaymentDateTimeCirca(tx.getPaymentDateTimeCirca());
    txXml.setCryptoCurrency(tx.getCryptoCurrency());
    txXml.setCryptoCurrencyUnits(tx.getCryptoCurrencyUnits());
    txXml.setAccountSend(tx.getAccountSend());
    txXml.setAccountReceive(tx.getAccountReceive());
    txXml.setEventRef(tx.getEventRef());
    txXml.setPersonSendRef(tx.getPersonSendRef());
    txXml.setPersonReceiveRef(tx.getPersonReceiveRef());
    relationsXml.getFinancialTransactions().add(txXml);
  }

  private Ech0051DocumentXml.MarkedValueXml buildMarkedValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    Ech0051DocumentXml.MarkedValueXml markedValueXml = new Ech0051DocumentXml.MarkedValueXml();
    markedValueXml.setMarking(value);
    return markedValueXml;
  }

  private StreetParts splitStreetAndNumber(String streetLine) {
    if (streetLine == null || streetLine.isBlank()) {
      return new StreetParts(null, null);
    }
    String trimmed = streetLine.trim();
    int tailStart = findLastSegmentStart(trimmed);
    if (tailStart <= 0) {
      return new StreetParts(trimmed, null);
    }

    String houseNumberCandidate = trimmed.substring(tailStart);
    if (!startsWithDigit(houseNumberCandidate)) {
      return new StreetParts(trimmed, null);
    }

    int separatorStart = tailStart - 1;
    while (separatorStart >= 0 && isStreetSeparator(trimmed.charAt(separatorStart))) {
      separatorStart--;
    }
    if (separatorStart == tailStart - 1) {
      return new StreetParts(trimmed, null);
    }

    String street = trimmed.substring(0, separatorStart + 1).trim();
    return street.isEmpty() ? new StreetParts(trimmed, null) : new StreetParts(street, houseNumberCandidate);
  }

  private int findLastSegmentStart(String value) {
    int index = value.length() - 1;
    while (index >= 0 && isHouseNumberChar(value.charAt(index))) {
      index--;
    }
    return index + 1;
  }

  private boolean isHouseNumberChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '/' || c == '-';
  }

  private boolean isStreetSeparator(char c) {
    return Character.isWhitespace(c) || c == ',';
  }

  private boolean startsWithDigit(String value) {
    return !value.isEmpty() && Character.isDigit(value.charAt(0));
  }

  private record StreetParts(String street, String houseNumber) {
  }

  private String resolveDocumentLanguage(Ech0051DocumentPayload documentDto) {
    if (documentDto == null || documentDto.getPersons() == null) {
      return Ech051Constants.DEFAULT_LANGUAGE;
    }
    return documentDto.getPersons().stream().map(Ech0051DocumentPayload.Person::getNaturalIdentity).filter(Objects::nonNull).map(Ech0051DocumentPayload.NaturalIdentity::getLanguageCode).filter(Objects::nonNull).findFirst().orElse(Ech051Constants.DEFAULT_LANGUAGE);
  }

  private Ech0051DocumentXml.UsageXml buildUsage(String label, String code) {
    Ech0051DocumentXml.UsageXml usageXml = new Ech0051DocumentXml.UsageXml();
    usageXml.setMarking(buildMarkingWithLang(label));
    Ech0051DocumentXml.SourceIdXml sourceId = new Ech0051DocumentXml.SourceIdXml();
    sourceId.setSource(RIPOL_SOURCE);
    sourceId.setSourceTable(CommunicationUsageLabels.SOURCE_TABLE);
    sourceId.setValue(code);
    usageXml.setSourceId(sourceId);
    return usageXml;
  }

  private Ech0051DocumentXml.UsageXml buildUsageWithoutSource(String label) {
    Ech0051DocumentXml.UsageXml usageXml = new Ech0051DocumentXml.UsageXml();
    usageXml.setMarking(buildMarkingWithLang(label));
    return usageXml;
  }

  private Ech0051DocumentXml.PhoneXml buildPhone(String number, String usageLabel, String usageCode) {
    Ech0051DocumentXml.PhoneXml phoneXml = new Ech0051DocumentXml.PhoneXml();
    phoneXml.setUsage(buildUsage(usageLabel, usageCode));
    phoneXml.setPhoneNumberInternational(number);
    return phoneXml;
  }

  private String resolveMessageType(Ech0051DocumentPayload documentDto) {
    if (isCyberAchatNonRecu(documentDto)) {
      return Ech051Constants.MessageTypes.CYBER_ACHAT_NON_RECU;
    }
    if (isCyberFausseAnnonce(documentDto)) {
      return Ech051Constants.MessageTypes.CYBER_FAUSSE_ANNONCE;
    }
    if (isCyberCommandeFrauduleuse(documentDto)) {
      return Ech051Constants.MessageTypes.CYBER_COMMANDE_FRAUDULEUSE;
    }

    if (isDommage(documentDto)) {
      return Ech051Constants.MessageTypes.DOMMAGE;
    }

    boolean hasVehicle = documentDto != null && documentDto.getVehicles() != null && !documentDto.getVehicles().isEmpty();
    if (isVol(documentDto) && hasVehicle) {
      return Ech051Constants.MessageTypes.VELO_MOFA;
    }
    if (isVol(documentDto)) {
      return Ech051Constants.MessageTypes.VOL;
    }

    return defaultMessageType;
  }

  private boolean isDommage(Ech0051DocumentPayload documentDto) {
    if (documentDto == null) {
      return false;
    }
    Ech0051DocumentPayload.ProcessData processData = documentDto.getProcessData();
    if (processData != null && Ech051Constants.SourceIds.DOMMAGE_MATERIEL.equals(processData.getSourceId())) {
      return true;
    }
    return hasAnyTypeOfCrime(documentDto, Ech051Constants.TYPE_OF_CRIME_DOMMAGE_CODE);
  }

  private boolean isVol(Ech0051DocumentPayload documentDto) {
    if (documentDto == null) {
      return false;
    }
    Ech0051DocumentPayload.ProcessData processData = documentDto.getProcessData();
    if (processData != null && Ech051Constants.SourceIds.VOL.equals(processData.getSourceId())) {
      return true;
    }
    return hasAnyTypeOfCrime(documentDto, Ech051Constants.TYPE_OF_CRIME_VOL_CODE);
  }

  private boolean hasAnyTypeOfCrime(Ech0051DocumentPayload documentDto, String code) {
    if (documentDto == null || documentDto.getEvents() == null || code == null) {
      return false;
    }
    return documentDto.getEvents().stream()
        .map(Ech0051DocumentPayload.Event::getTypeOfCrime)
        .filter(Objects::nonNull)
        .map(Ech0051DocumentPayload.RipolReference::getCode)
        .anyMatch(code::equals);
  }

  private boolean isCyberCommandeFrauduleuse(Ech0051DocumentPayload documentDto) {
    if (documentDto == null) {
      return false;
    }

    Ech0051DocumentPayload.ProcessData processData = documentDto.getProcessData();
    if (processData != null && Ech051Constants.SourceIds.CYBERCRIME_COMMANDE_FRAUDULEUSE.equals(processData.getSourceId())) {
      return true;
    }

    return documentDto.getEvents() != null
        && documentDto.getEvents().stream()
            .map(Ech0051DocumentPayload.Event::getTypeOfCrime)
            .filter(Objects::nonNull)
            .map(Ech0051DocumentPayload.RipolReference::getCode)
            .anyMatch(Ech051Constants.TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_CODE::equals);
  }

  private boolean isCyberFausseAnnonce(Ech0051DocumentPayload documentDto) {
    if (documentDto == null) {
      return false;
    }

    Ech0051DocumentPayload.ProcessData processData = documentDto.getProcessData();
    if (processData != null && Ech051Constants.SourceIds.CYBERCRIME_FAUSSE_ANNONCE.equals(processData.getSourceId())) {
      return true;
    }

    return documentDto.getEvents() != null
        && documentDto.getEvents().stream()
            .map(Ech0051DocumentPayload.Event::getTypeOfCrime)
            .filter(Objects::nonNull)
            .map(Ech0051DocumentPayload.RipolReference::getCode)
            .anyMatch(Ech051Constants.TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_CODE::equals);
  }

  private boolean isCyberAchatNonRecu(Ech0051DocumentPayload documentDto) {
    if (documentDto == null) {
      return false;
    }

    Ech0051DocumentPayload.ProcessData processData = documentDto.getProcessData();
    if (processData != null && Ech051Constants.SourceIds.CYBERCRIME_ACHAT_NON_RECU.equals(processData.getSourceId())) {
      return true;
    }

    return documentDto.getEvents() != null
        && documentDto.getEvents().stream()
            .map(Ech0051DocumentPayload.Event::getTypeOfCrime)
            .filter(Objects::nonNull)
            .map(Ech0051DocumentPayload.RipolReference::getCode)
            .anyMatch(Ech051Constants.TYPE_OF_CRIME_CYBER_ACHAT_NON_RECU_CODE::equals);
  }
}

