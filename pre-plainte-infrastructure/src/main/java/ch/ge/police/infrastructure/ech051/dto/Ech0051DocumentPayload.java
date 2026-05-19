package ch.ge.police.infrastructure.ech051.dto;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * Représentation du document eCH-0051 complet : c'est le contrat interne
 * avant la sérialisation SEP.
 */
@Value
@Builder(toBuilder = true)
public class Ech0051DocumentPayload {

  @Value
  @Builder
  public static class RipolReference {
    String code;
    String label;
    String sourceTable;
    String source;

    public static RipolReference of(String code, String label, String sourceTable) {
      return RipolReference.builder()
          .code(code)
          .label(label)
          .sourceTable(sourceTable)
          .source("RIPOL")
          .build();
    }

    public static RipolReference ofIso(String code, String label, String sourceTable) {
      return RipolReference.builder()
          .code(code)
          .label(label)
          .sourceTable(sourceTable)
          .source("ISO")
          .build();
    }
  }
  ProcessData processData;
  @Singular List<Person> persons;
  @Singular List<Event> events;
  @Singular List<ObjectItem> objects;
  @Singular List<VehicleItem> vehicles;
  @Singular List<BusinessCase> businessCases;
  Relations relations;

  @Value
  @Builder(toBuilder = true)
  public static class ProcessData {
    String deliveryDate;
    String sourceId;
    String sourceValue;
    String processingStatus;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Person {
    String key;
    PersonType type;
    NaturalIdentity naturalIdentity;
    LegalIdentity legalIdentity;
    Address address;
    Communication communication;
    Boolean deliveredAbroad;
    String additionalInformation;
  }

  public enum PersonType {
    NATURAL,
    LEGAL
  }

  @Value
  @Builder(toBuilder = true)
  public static class NaturalIdentity {
    String key;
    String identityCategory;
    String additionalInformation;
    String officialName;
    String originalName;
    String firstName;
    String callName;
    RipolReference sex;
    String birthDate;
    String languageCode;
    RipolLocation placeOfOrigin;
    String socialSecurityNumber;
    RipolReference nationality;
    String profession;
  }

  /**
   * Représentation générique d'un lieu géographique RIPOL.
   * Utilisé pour les lieux d'origine, localités et pays.
   * Pour les pays, zipCode est null.
   */
  @Value
  @Builder(toBuilder = true)
  public static class RipolLocation {
    String code;
    String label;
    String sourceTable;
    String zipCode;
  }

  @Value
  @Builder(toBuilder = true)
  public static class LegalIdentity {
    String currentName;
    String typeOrganisation;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Address {
    String street;
    String houseNumber;
    RipolLocation place;
    RipolLocation country;
    String additional;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Communication {
    String email;
    String mobile;
    String phone;
    String uri;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Event {
    String key;
    String descriptionShort;
    String complaintDate;
    ActionPeriod actionPeriod;
    ActionPlace actionPlace;
    ActionPlace secondaryActionPlace;
    String bootyAmount;
    RipolReference locality;
    RipolReference modeOperandi;
    RipolReference typeOfCrime;
    String facts;
    String additionalInformation;
  }

  @Value
  @Builder(toBuilder = true)
  public static class ActionPeriod {
    String from;
    String to;
  }

  @Value
  @Builder(toBuilder = true)
  public static class ActionPlace {
    String street;
    String houseNumber;
    RipolLocation place;
    String cityArea;
    RipolLocation country;
  }

  @Value
  @Builder(toBuilder = true)
  public static class ObjectItem {
    String key;
    RipolReference typeOfObject;
    String description;
    RipolReference fabricant;
    String fabricantAutre;
    RipolReference modele;
    String modeleAutre;
    String material;
    RipolReference couleur;
    RipolReference couleurSecondaire;
    String realValue;
    String purchaseDate;
    String numeroSerie;
    String gravure;
    Identification identification;
    String additionalInformation;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Identification {
    String type;
    String number;
  }

  @Value
  @Builder(toBuilder = true)
  public static class VehicleItem {
    String key;
    RipolReference typeOfVehicle;
    String vin;
    String frameNumber;
    RipolReference mark;
    String markOther;
    RipolReference modelType;
    String modelOther;
    RipolReference colour;
    RipolReference colourSecondary;
    String velofinderId;
    String purchaseDate;
    String additionalInformation;
    NumberPlate numberPlate;
  }

  /**
   * Représente une plaque d'immatriculation avec numéro, pays et canton.
   */
  @Value
  @Builder(toBuilder = true)
  public static class NumberPlate {
    String number;
    RipolLocation country;
    RipolLocation canton;
  }

  @Value
  @Builder(toBuilder = true)
  public static class BusinessCase {
    String key;
    String caseNumber;
    List<File> file;
  }

  @Value
  @Builder(toBuilder = true)
  public static class File {
    List<Attachment> attachment;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Attachment {
    String filename;
    String content;
  }

  @Value
  @Builder(toBuilder = true)
  public static class Relations {
    @Singular List<InvolvedParty> involvedParties;
    @Singular List<PersonLink> personLinks;
    @Singular List<FinancialTransaction> financialTransactions;
    @Singular List<EventBusinessCaseLink> eventBusinessCaseLinks;
    @Singular List<EventObjectLink> eventObjectLinks;
    @Singular List<EventVehicleLink> eventVehicleLinks;
    @Singular List<ObjectPersonLink> objectPersonLinks;
    @Singular List<VehiclePersonLink> vehiclePersonLinks;
  }

  @Value
  @Builder(toBuilder = true)
  public static class FinancialTransaction {
    String paymentType;
    String transactionNumber;
    String platformType;
    String platformId;
    String paymentDateTime;
    String paymentDateTimeCirca;
    String cryptoCurrency;
    String cryptoCurrencyUnits;
    String accountSend;
    String accountReceive;
    String eventRef;
    String personSendRef;
    String personReceiveRef;
  }

  @Value
  @Builder(toBuilder = true)
  public static class InvolvedParty {
    String businessCaseRef;
    RipolReference typeOfInvolvement;
    String personRef;
    String eventRef;
  }

  @Value
  @Builder(toBuilder = true)
  public static class PersonLink {
    RipolReference person1Role;
    String person1Ref;
    RipolReference person2Role;
    String person2Ref;
  }

  @Value
  @Builder(toBuilder = true)
  public static class ObjectPersonLink {
    String objectRef;
    String personRef;
    String insurerRef;
    RipolReference personRole;
  }

  @Value
  @Builder(toBuilder = true)
  public static class EventBusinessCaseLink {
    String eventRef;
    String businessCaseRef;
  }

  @Value
  @Builder(toBuilder = true)
  public static class EventObjectLink {
    String eventRef;
    String objectRef;
    RipolReference objectRole;
  }

  @Value
  @Builder(toBuilder = true)
  public static class EventVehicleLink {
    String eventRef;
    String vehicleRef;
    RipolReference vehicleRole;
  }

  @Value
  @Builder(toBuilder = true)
  public static class VehiclePersonLink {
    String vehicleRef;
    String personRef;
    String insurerRef;
    RipolReference personRole;
  }
}
