package ch.ge.police.infrastructure.ech051.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Version JAXB complète du document eCH-0051. Chaque sous-classe correspond à une
 * section du schéma (processData, persons, objects, events, businessCases, relations)
 * afin de permettre un marshalling fidèle depuis les DTO core vers l'XML.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"processData", "persons", "vehicles", "objects", "events", "businessCases", "relations"})
public class Ech0051DocumentXml {

  public Ech0051DocumentXml() {
    // JAXB requires an explicit no-arg constructor.
      }

  @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
  private String xmlLang;

  @XmlElement(name = "processData", namespace = SepNamespaces.ECH_0051_NS)
  private ProcessDataXml processData;

  @XmlElement(name = "person", namespace = SepNamespaces.ECH_0051_NS)
  private List<PersonXml> persons = new ArrayList<>();

  @XmlElement(name = "vehicle", namespace = SepNamespaces.ECH_0051_NS)
  private List<VehicleXml> vehicles = new ArrayList<>();

  @XmlElement(name = "object", namespace = SepNamespaces.ECH_0051_NS)
  private List<ObjectXml> objects = new ArrayList<>();

  @XmlElement(name = "event", namespace = SepNamespaces.ECH_0051_NS)
  private List<EventXml> events = new ArrayList<>();

  @XmlElement(name = "businessCase", namespace = SepNamespaces.ECH_0051_NS)
  private List<BusinessCaseXml> businessCases = new ArrayList<>();

  @XmlElement(name = "relations", namespace = SepNamespaces.ECH_0051_NS)
  private RelationsXml relations = new RelationsXml();

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ProcessDataXml {
    public ProcessDataXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "version", namespace = SepNamespaces.SEP_NS)
    private String version = "2";

    @XmlElement(name = "deliveryDate", namespace = SepNamespaces.ECH_0051_NS)
    private String deliveryDate;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;

    @XmlElement(name = "processingStatus", namespace = SepNamespaces.ECH_0051_NS)
    private ProcessingStatusXml processingStatus;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ProcessingStatusXml {
    public ProcessingStatusXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class PersonXml {
    public PersonXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "natural", namespace = SepNamespaces.ECH_0051_NS)
    private NaturalXml natural;

    @XmlElement(name = "legal", namespace = SepNamespaces.ECH_0051_NS)
    private LegalXml legal;

    @XmlElement(name = "address", namespace = SepNamespaces.ECH_0051_NS)
    private AddressXml address;

    @XmlElement(name = "meansOfCommunication", namespace = SepNamespaces.ECH_0051_NS)
    private List<CommunicationXml> communications = new ArrayList<>();

    @XmlAttribute(name = "deliveredAbroad", namespace = SepNamespaces.SEP_NS)
    private String deliveredAbroad;

    @XmlElement(name = "additionalInformation", namespace = SepNamespaces.ECH_0051_NS)
    private String additionalInformation;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class NaturalXml {
    public NaturalXml() {
      // JAXB requires an explicit no-arg constructor.
          }


    @XmlElement(name = "identity", namespace = SepNamespaces.ECH_0051_NS)
    private NaturalIdentityXml identity;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class NaturalIdentityXml {
    public NaturalIdentityXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "identityCategory", namespace = SepNamespaces.ECH_0051_NS)
    private IdentityCategoryXml identityCategory;

    @XmlElement(name = "additionalInformation", namespace = SepNamespaces.ECH_0051_NS)
    private String additionalInformation;

    @XmlElement(name = "officialName", namespace = SepNamespaces.ECH_0051_NS)
    private String officialName;

    @XmlElement(name = "originalName", namespace = SepNamespaces.ECH_0051_NS)
    private String originalName;

    @XmlElement(name = "firstName", namespace = SepNamespaces.ECH_0051_NS)
    private String firstName;

    @XmlElement(name = "callName", namespace = SepNamespaces.ECH_0051_NS)
    private String callName;

    @XmlElement(name = "sex", namespace = SepNamespaces.ECH_0051_NS)
    private SexXml sex;

    @XmlElement(name = "birthData", namespace = SepNamespaces.ECH_0051_NS)
    private BirthDataXml birthData;

    @XmlElement(name = "language", namespace = SepNamespaces.ECH_0051_NS)
    private LanguageXml language;

    @XmlElement(name = "placeOfOrigin", namespace = SepNamespaces.ECH_0051_NS)
    private PlaceOfOriginXml placeOfOrigin;

    @XmlElement(name = "socialSecurityNumber", namespace = SepNamespaces.ECH_0051_NS)
    private String socialSecurityNumber;

    @XmlElement(name = "nationality", namespace = SepNamespaces.ECH_0051_NS)
    private NationalityXml nationality;

    @XmlElement(name = "profession", namespace = SepNamespaces.ECH_0051_NS)
    private String profession;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class IdentityCategoryXml {
    public IdentityCategoryXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private String marking;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class PlaceOfOriginXml {
    public PlaceOfOriginXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;

    @XmlElement(name = "zipCode", namespace = SepNamespaces.ECH_0051_NS)
    private String zipCode;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class BirthDataXml {
    public BirthDataXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "date", namespace = SepNamespaces.ECH_0051_NS)
    private String date;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class LanguageXml {
    public LanguageXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "languageIsoCode", namespace = SepNamespaces.ECH_0051_NS)
    private String languageIsoCode;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SexXml {
    public SexXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class NationalityXml {
    public NationalityXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class MarkingWithLangXml {
    public MarkingWithLangXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    private String lang = "fr";

    @XmlValue
    private String value;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SourceIdXml {
    public SourceIdXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "sourceTable")
    private String sourceTable;

    @XmlValue
    private String value;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class RipolValueXml {
    public RipolValueXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class LegalXml {
    public LegalXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "currentName", namespace = SepNamespaces.ECH_0051_NS)
    private String currentName;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AddressXml {
    public AddressXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "street", namespace = SepNamespaces.ECH_0051_NS)
    private MarkedValueXml street;

    @XmlElement(name = "houseNumber", namespace = SepNamespaces.ECH_0051_NS)
    private String houseNumber;

    @XmlElement(name = "place", namespace = SepNamespaces.ECH_0051_NS)
    private AddressPlaceXml place;

    @XmlElement(name = "country", namespace = SepNamespaces.ECH_0051_NS)
    private CountryXml country;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AddressPlaceXml {
    public AddressPlaceXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;

    @XmlElement(name = "zipCode", namespace = SepNamespaces.ECH_0051_NS)
    private String zipCode;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class CountryXml {
    public CountryXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class MarkedValueXml {
    public MarkedValueXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private String marking;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class CommunicationXml {
    public CommunicationXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "eMail", namespace = SepNamespaces.ECH_0051_NS)
    private EmailXml email;

    @XmlElement(name = "mobile", namespace = SepNamespaces.ECH_0051_NS)
    private PhoneXml mobile;

    @XmlElement(name = "phone", namespace = SepNamespaces.ECH_0051_NS)
    private PhoneXml phone;

    @XmlElement(name = "uri", namespace = SepNamespaces.ECH_0051_NS)
    private UriXml uri;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EmailXml {
    public EmailXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "usage", namespace = SepNamespaces.ECH_0051_NS)
    private UsageXml usage;

    @XmlElement(name = "eMailAddress", namespace = SepNamespaces.ECH_0051_NS)
    private String emailAddress;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class PhoneXml {
    public PhoneXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "usage", namespace = SepNamespaces.ECH_0051_NS)
    private UsageXml usage;

    @XmlElement(name = "phoneNumberInternational", namespace = SepNamespaces.ECH_0051_NS)
    private String phoneNumberInternational;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class UriXml {
    public UriXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "usage", namespace = SepNamespaces.ECH_0051_NS)
    private UsageXml usage;

    @XmlElement(name = "uri", namespace = SepNamespaces.ECH_0051_NS)
    private String uri;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class UsageXml {
    public UsageXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EventXml {
    public EventXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "descriptionShort", namespace = SepNamespaces.ECH_0051_NS)
    private String descriptionShort;

    @XmlElement(name = "complaintDate", namespace = SepNamespaces.ECH_0051_NS)
    private String complaintDate;

    @XmlElement(name = "actionPlaceGroup", namespace = SepNamespaces.ECH_0051_NS)
    private List<ActionPlaceGroupXml> actionPlaceGroups = new ArrayList<>();

    @XmlElement(name = "bootyAmount", namespace = SepNamespaces.ECH_0051_NS)
    private BootyAmountXml bootyAmount;

    @XmlElement(name = "locality", namespace = SepNamespaces.ECH_0051_NS)
    private LocalityXml locality;

    @XmlElement(name = "modeOperandi", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml modeOperandi;

    @XmlElement(name = "typeOfCrime", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml typeOfCrime;

    @XmlElement(name = "facts", namespace = SepNamespaces.ECH_0051_NS)
    private String facts;

    @XmlElement(name = "additionalInformation", namespace = SepNamespaces.ECH_0051_NS)
    private String additionalInformation;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class BootyAmountXml {
    public BootyAmountXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "amount", namespace = SepNamespaces.ECH_0051_NS)
    private String amount;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class LocalityXml {
    public LocalityXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "localityCode", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml localityCode;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ActionPlaceGroupXml {
    public ActionPlaceGroupXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "actionPeriodFromUnreliable", namespace = SepNamespaces.SEP_NS)
    private String actionPeriodFromUnreliable = "false";

    @XmlAttribute(name = "actionPeriodToUnreliable", namespace = SepNamespaces.SEP_NS)
    private String actionPeriodToUnreliable = "false";

    @XmlElement(name = "mainRecord", namespace = SepNamespaces.ECH_0051_NS)
    private String mainRecord;

    @XmlElement(name = "actionPlace", namespace = SepNamespaces.ECH_0051_NS)
    private ActionPlaceXml actionPlace;

    @XmlElement(name = "actionPeriodFrom", namespace = SepNamespaces.ECH_0051_NS)
    private String actionPeriodFrom;

    @XmlElement(name = "actionPeriodTo", namespace = SepNamespaces.ECH_0051_NS)
    private String actionPeriodTo;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ActionPlaceXml {
    public ActionPlaceXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "street", namespace = SepNamespaces.ECH_0051_NS)
    private MarkedValueXml street;

    @XmlElement(name = "houseNumber", namespace = SepNamespaces.ECH_0051_NS)
    private String houseNumber;

    @XmlElement(name = "place", namespace = SepNamespaces.ECH_0051_NS)
    private AddressPlaceXml place;

    @XmlElement(name = "cityArea", namespace = SepNamespaces.ECH_0051_NS)
    private MarkedValueXml cityArea;

    @XmlElement(name = "country", namespace = SepNamespaces.ECH_0051_NS)
    private CountryXml country;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class VehicleXml {
    public VehicleXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "additionalInformation", namespace = SepNamespaces.ECH_0051_NS)
    private String additionalInformation;

    @XmlElement(name = "definition", namespace = SepNamespaces.ECH_0051_NS)
    private VehicleDefinitionXml definition;

    @XmlElement(name = "numberPlate", namespace = SepNamespaces.ECH_0051_NS)
    private NumberPlateXml numberPlate;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class NumberPlateXml {
    public NumberPlateXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "number", namespace = SepNamespaces.ECH_0051_NS)
    private String number;

    @XmlElement(name = "country", namespace = SepNamespaces.ECH_0051_NS)
    private CountryXml country;

    @XmlElement(name = "canton", namespace = SepNamespaces.ECH_0051_NS)
    private CantonXml canton;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class CantonXml {
    public CantonXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "marking", namespace = SepNamespaces.ECH_0051_NS)
    private MarkingWithLangXml marking;

    @XmlElement(name = "sourceID", namespace = SepNamespaces.ECH_0051_NS)
    private SourceIdXml sourceId;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class VehicleDefinitionXml {
    public VehicleDefinitionXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "velofinderId", namespace = SepNamespaces.SEP_NS)
    private String velofinderId;

    @XmlAttribute(name = "purchaseDate", namespace = SepNamespaces.SEP_NS)
    private String purchaseDate;

    @XmlAttribute(name = "frameNumber", namespace = SepNamespaces.SEP_NS)
    private String frameNumber;

    @XmlAttribute(name = "markOther", namespace = SepNamespaces.SEP_NS)
    private String markOther;

    @XmlAttribute(name = "modelOther", namespace = SepNamespaces.SEP_NS)
    private String modelOther;

    @XmlElement(name = "vin", namespace = SepNamespaces.ECH_0051_NS)
    private String vin;

    @XmlElement(name = "typeOfVehicleGroup", namespace = SepNamespaces.ECH_0051_NS)
    private TypeOfVehicleGroupXml typeOfVehicleGroup;

    @XmlElement(name = "mark", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml mark;

    @XmlElement(name = "modelType", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml modelType;

    @XmlElement(name = "colour", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml colour;

    @XmlElement(name = "colourSecondary", namespace = SepNamespaces.SEP_NS)
    private RipolValueXml colourSecondary;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class TypeOfVehicleGroupXml {
    public TypeOfVehicleGroupXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "typeOfVehicle", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml typeOfVehicle;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ObjectXml {
    public ObjectXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "additionalInformation", namespace = SepNamespaces.ECH_0051_NS)
    private String additionalInformation;

    @XmlElement(name = "definition", namespace = SepNamespaces.ECH_0051_NS)
    private ObjectDefinitionXml definition;

    @XmlElement(name = "identificationNumber", namespace = SepNamespaces.ECH_0051_NS)
    private IdentificationXml identificationNumber;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ObjectDefinitionXml {
    public ObjectDefinitionXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "gravure", namespace = SepNamespaces.SEP_NS)
    private String gravure;

    @XmlAttribute(name = "markOther", namespace = SepNamespaces.SEP_NS)
    private String markOther;

    @XmlAttribute(name = "modelOther", namespace = SepNamespaces.SEP_NS)
    private String modelOther;

    @XmlElement(name = "typeOfObject", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml typeOfObject;

    @XmlElement(name = "mark", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml mark;

    @XmlElement(name = "modelType", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml modelType;

    @XmlElement(name = "material", namespace = SepNamespaces.ECH_0051_NS)
    private MarkedValueXml material;

    @XmlElement(name = "colour", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml colour;

    @XmlElement(name = "colourSecondary", namespace = SepNamespaces.SEP_NS)
    private RipolValueXml colourSecondary;

    @XmlElement(name = "realValue", namespace = SepNamespaces.ECH_0051_NS)
    private RealValueXml realValue;

    @XmlElement(name = "purchaseDate", namespace = SepNamespaces.ECH_0051_NS)
    private String purchaseDate;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class RealValueXml {
    public RealValueXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "amount", namespace = SepNamespaces.ECH_0051_NS)
    private String amount;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class IdentificationXml {
    public IdentificationXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "type", namespace = SepNamespaces.SEP_NS)
    private String type;

    @XmlElement(name = "isUnique", namespace = SepNamespaces.ECH_0051_NS)
    private String isUnique;

    @XmlElement(name = "number", namespace = SepNamespaces.ECH_0051_NS)
    private String number;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class BusinessCaseXml {
    public BusinessCaseXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "key", namespace = SepNamespaces.ECH_0051_NS)
    private String key;

    @XmlElement(name = "caseNumber", namespace = SepNamespaces.ECH_0051_NS)
    private String caseNumber;

    @XmlElement(name = "file", namespace = SepNamespaces.ECH_0051_NS)
    private List<FileXml> file = new ArrayList<>();
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class FileXml {
    public FileXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "attachment", namespace = SepNamespaces.ECH_0051_NS)
    private List<AttachmentXml> attachment = new ArrayList<>();
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AttachmentXml {
    public AttachmentXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "filename", namespace = SepNamespaces.ECH_0051_NS)
    private String filename;

    @XmlElement(name = "content", namespace = SepNamespaces.ECH_0051_NS)
    private String content;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class RelationsXml {
    public RelationsXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "personLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<PersonLinkXml> personLinks = new ArrayList<>();

    @XmlElement(name = "financialTransaction", namespace = SepNamespaces.ECH_0051_NS)
    private List<FinancialTransactionXml> financialTransactions = new ArrayList<>();

    @XmlElement(name = "involvedParty", namespace = SepNamespaces.ECH_0051_NS)
    private List<InvolvedPartyXml> involvedParties = new ArrayList<>();

    @XmlElement(name = "eventBusinessCaseLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<EventBusinessCaseLinkXml> eventBusinessCaseLinks = new ArrayList<>();

    @XmlElement(name = "eventObjectLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<EventObjectLinkXml> eventObjectLinks = new ArrayList<>();

    @XmlElement(name = "eventVehicleLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<EventVehicleLinkXml> eventVehicleLinks = new ArrayList<>();

    @XmlElement(name = "objectPersonLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<ObjectPersonLinkXml> objectPersonLinks = new ArrayList<>();

    @XmlElement(name = "vehiclePersonLink", namespace = SepNamespaces.ECH_0051_NS)
    private List<VehiclePersonLinkXml> vehiclePersonLinks = new ArrayList<>();
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class FinancialTransactionXml {
    public FinancialTransactionXml() {
      // JAXB requires an explicit no-arg constructor.
    }

    @XmlAttribute(name = "paymentType", namespace = SepNamespaces.SEP_NS)
    private String paymentType;

    @XmlAttribute(name = "transactionNumber", namespace = SepNamespaces.SEP_NS)
    private String transactionNumber;

    @XmlAttribute(name = "platformType", namespace = SepNamespaces.SEP_NS)
    private String platformType;

    @XmlAttribute(name = "platformId", namespace = SepNamespaces.SEP_NS)
    private String platformId;

    @XmlAttribute(name = "paymentDateTime", namespace = SepNamespaces.SEP_NS)
    private String paymentDateTime;

    @XmlAttribute(name = "paymentDateTimeCirca", namespace = SepNamespaces.SEP_NS)
    private String paymentDateTimeCirca;

    @XmlAttribute(name = "cryptoCurrency", namespace = SepNamespaces.SEP_NS)
    private String cryptoCurrency;

    @XmlAttribute(name = "cryptoCurrencyUnits", namespace = SepNamespaces.SEP_NS)
    private String cryptoCurrencyUnits;

    @XmlElement(name = "accountSend", namespace = SepNamespaces.ECH_0051_NS)
    private String accountSend;

    @XmlElement(name = "accountReceive", namespace = SepNamespaces.ECH_0051_NS)
    private String accountReceive;

    @XmlElement(name = "eventRef", namespace = SepNamespaces.ECH_0051_NS)
    private String eventRef;

    @XmlElement(name = "personSendRef", namespace = SepNamespaces.ECH_0051_NS)
    private String personSendRef;

    @XmlElement(name = "personReceiveRef", namespace = SepNamespaces.ECH_0051_NS)
    private String personReceiveRef;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class InvolvedPartyXml {
    public InvolvedPartyXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "businessCaseRef", namespace = SepNamespaces.ECH_0051_NS)
    private String businessCaseRef;

    @XmlElement(name = "personRef", namespace = SepNamespaces.ECH_0051_NS)
    private String personRef;

    @XmlElement(name = "eventRef", namespace = SepNamespaces.ECH_0051_NS)
    private String eventRef;

    @XmlElement(name = "typeOfInvolvement", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml typeOfInvolvement;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class PersonLinkXml {
    public PersonLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "person1Role", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml person1Role;

    @XmlElement(name = "person1Ref", namespace = SepNamespaces.ECH_0051_NS)
    private String person1Ref;

    @XmlElement(name = "person2Role", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml person2Role;

    @XmlElement(name = "person2Ref", namespace = SepNamespaces.ECH_0051_NS)
    private String person2Ref;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ObjectPersonLinkXml {
    public ObjectPersonLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlAttribute(name = "insurerRef", namespace = SepNamespaces.SEP_NS)
    private String insurerRef;

    @XmlElement(name = "objectRef", namespace = SepNamespaces.ECH_0051_NS)
    private String objectRef;

    @XmlElement(name = "personRole", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml personRole;

    @XmlElement(name = "personRef", namespace = SepNamespaces.ECH_0051_NS)
    private String personRef;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EventBusinessCaseLinkXml {
    public EventBusinessCaseLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "eventRef", namespace = SepNamespaces.ECH_0051_NS)
    private String eventRef;

    @XmlElement(name = "businessCaseRef", namespace = SepNamespaces.ECH_0051_NS)
    private String businessCaseRef;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EventObjectLinkXml {
    public EventObjectLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "eventRef", namespace = SepNamespaces.ECH_0051_NS)
    private String eventRef;

    @XmlElement(name = "objectRole", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml objectRole;

    @XmlElement(name = "objectRef", namespace = SepNamespaces.ECH_0051_NS)
    private String objectRef;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EventVehicleLinkXml {
    public EventVehicleLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "eventRef", namespace = SepNamespaces.ECH_0051_NS)
    private String eventRef;

    @XmlElement(name = "vehicleRole", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml vehicleRole;

    @XmlElement(name = "vehicleRef", namespace = SepNamespaces.ECH_0051_NS)
    private String vehicleRef;
  }

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class VehiclePersonLinkXml {
    public VehiclePersonLinkXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "vehicleRef", namespace = SepNamespaces.ECH_0051_NS)
    private String vehicleRef;

    @XmlElement(name = "personRole", namespace = SepNamespaces.ECH_0051_NS)
    private RipolValueXml personRole;

    @XmlElement(name = "personRef", namespace = SepNamespaces.ECH_0051_NS)
    private String personRef;

    @XmlElement(name = "insurerRef", namespace = SepNamespaces.ECH_0051_NS)
    private String insurerRef;
  }
}

