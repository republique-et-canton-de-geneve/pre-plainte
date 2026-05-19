package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.MoyenPaiement;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.EventBusinessCaseLink;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.EventObjectLink;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.EventVehicleLink;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.FinancialTransaction;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.InvolvedParty;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectPersonLink;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonType;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonLink;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Relations;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehiclePersonLink;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper dédié à la construction des relations entre les différentes entités
 * (personnes, événements, objets, véhicules, dossiers) au format eCH-0051.
 */
@Component
public class SuisseEpoliceRelationsMapper {
  private static final int THIRD_PERSON_INDEX = 2;

  /**
   * Construit l'ensemble des relations entre les entités.
   *
   * @param persons liste des personnes
   * @param events liste des événements
   * @param objects liste des objets
   * @param vehicles liste des véhicules
   * @param businessCase le dossier
   * @param declarationType type de déclaration (INDIVIDUAL, TIERS)
   */
  public Relations buildRelations(List<Person> persons, List<Event> events,
                                   List<ObjectItem> objects, List<VehicleItem> vehicles,
                                   BusinessCase businessCase, DeclarationType declarationType,
                                   IncidentBase incident) {
    var relationsBuilder = Relations.builder();

    String eventRef = events.stream().findFirst().map(Event::getKey).orElse(null);
    String businessCaseRef = businessCase != null ? businessCase.getKey() : null;

    if (incident instanceof Cybercrime cybercrime
        && isCyberTransactionType(cybercrime)) {
      buildCyberCommandeFrauduleuseRelations(persons, eventRef, businessCaseRef, objects, incident, relationsBuilder);
      buildEventBusinessCaseLink(eventRef, businessCaseRef, relationsBuilder);
      return relationsBuilder.build();
    }

    switch (declarationType) {
      case TIERS -> buildTiersRelations(persons, eventRef, businessCaseRef, objects, vehicles, relationsBuilder);
      case ENTREPRISE -> buildEntrepriseRelations(persons, eventRef, businessCaseRef, objects, vehicles, relationsBuilder);
      default -> buildIndividualRelations(persons, eventRef, businessCaseRef, objects, vehicles, relationsBuilder);
    }

    buildEventBusinessCaseLink(eventRef, businessCaseRef, relationsBuilder);

    return relationsBuilder.build();
  }

  private void buildCyberCommandeFrauduleuseRelations(
      List<Person> persons,
      String eventRef,
      String businessCaseRef,
      List<ObjectItem> objects,
      IncidentBase incident,
      Relations.RelationsBuilder builder
  ) {
    String victimRef = findPersonRefByKey(persons, Ech051Constants.PERSON_KEY_TIERS);
    String accusedRef = findCyberAccusedPersonRef(persons, victimRef);
    buildInvolvedPartyVictim(victimRef, eventRef, businessCaseRef, builder);
    buildInvolvedPartyAccused(accusedRef, eventRef, businessCaseRef, builder);
    buildCyberVictimAccusedPersonLink(victimRef, accusedRef, builder);

    ObjectItem identityObject = findObjectByKey(objects, Ech051Constants.OBJECT_KEY_TIERS);
    if (identityObject != null && victimRef != null) {
      builder.objectPersonLink(
          ObjectPersonLink.builder()
              .objectRef(identityObject.getKey())
              .insurerRef(Ech051Constants.INSURER_REF_CYBER)
              .personRef(victimRef)
              .build()
      );
    }

    ObjectItem transactionObject = findObjectByKey(objects, Ech051Constants.OBJECT_KEY_CYBER_TRANSACTION);
    if (transactionObject != null) {
      buildEventObjectLink(transactionObject, eventRef, builder);
      if (victimRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(transactionObject.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.INSURER_REF_CYBER)
                .personRef(victimRef)
                .build()
        );
      }
    }

    buildFinancialTransaction(incident, eventRef, victimRef, accusedRef, builder);
  }

  private void buildFinancialTransaction(
      IncidentBase incident,
      String eventRef,
      String victimRef,
      String accusedRef,
      Relations.RelationsBuilder builder
  ) {
    if (!(incident instanceof Cybercrime cybercrime)) {
      return;
    }
    AchatNonRecu achat = cybercrime.getAchatNonRecu();
    if (achat == null || achat.getMoyenPaiement() == null) {
      return;
    }

    FinancialTransaction.FinancialTransactionBuilder tx = FinancialTransaction.builder()
        .paymentType(resolvePaymentType(achat.getMoyenPaiement(), achat.getMoyenPaiementAutre()))
        .transactionNumber(resolveTransactionNumber(achat))
        .platformType(resolvePlatformType(achat))
        .platformId(achat.getPlateformeId())
        .paymentDateTime(toSepDateTime(achat.getDateOperation()))
        .paymentDateTimeCirca("false")
        .accountSend("inconnu")
        .eventRef(eventRef)
        .personSendRef(victimRef)
        .personReceiveRef(accusedRef);

    switch (achat.getMoyenPaiement()) {
      case IBAN -> tx.accountReceive(normalizeIbanForSep(achat.getIbanBeneficiaire()));
      case PAYPAL -> tx.accountReceive(trimToNull(achat.getComptePaypalBeneficiaire()));
      case TWINT -> tx.accountReceive(trimToNull(achat.getNumeroTwintBeneficiaire()));
      case CRYPTO -> {
        tx.accountReceive(trimToNull(achat.getAdresseWalletCrypto()));
        tx.cryptoCurrencyUnits(trimToNull(achat.getMontantDelitAchatLigne()));
      }
      case AUTRE -> tx.accountReceive(trimToNull(achat.getMoyenPaiementAutre()));
    }

    builder.financialTransaction(tx.build());
  }

  private String resolvePaymentType(MoyenPaiement moyenPaiement, String moyenPaiementAutre) {
    if (moyenPaiement == null) {
      return null;
    }
    return switch (moyenPaiement) {
      case IBAN -> "IBAN";
      case PAYPAL -> "PAYPAL";
      case TWINT -> "TWINT";
      case CRYPTO -> "CRYPTO";
      case AUTRE -> (moyenPaiementAutre == null || moyenPaiementAutre.isBlank()) ? "AUTRE" : moyenPaiementAutre;
    };
  }

  private String resolvePlatformType(AchatNonRecu achat) {
    if (achat == null || achat.getPlateformeUtilisee() == null) {
      return null;
    }
    if (achat.getPlateformeUtilisee().getLabel() != null && !achat.getPlateformeUtilisee().getLabel().isBlank()) {
      return achat.getPlateformeUtilisee().getLabel();
    }
    return achat.getPlateformeUtilisee().getCode();
  }

  private String resolveTransactionNumber(AchatNonRecu achat) {
    if (achat == null || achat.getMoyenPaiement() == null) {
      return null;
    }
    return switch (achat.getMoyenPaiement()) {
      case TWINT -> achat.getNumeroTwintBeneficiaire();
      case PAYPAL -> achat.getComptePaypalBeneficiaire();
      case CRYPTO -> achat.getHashTransactionCrypto();
      case IBAN, AUTRE -> null;
    };
  }

  private String toSepDateTime(String dateValue) {
    if (dateValue == null || dateValue.isBlank()) {
      return null;
    }
    try {
      java.time.LocalDate date = java.time.LocalDate.parse(dateValue);
      return date.atStartOfDay(java.time.ZoneId.of("Europe/Zurich"))
          .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS+01:00"));
    } catch (Exception ignored) {
      return dateValue;
    }
  }

  private String findPersonRefByKey(List<Person> persons, String key) {
    return persons.stream()
        .filter(p -> p != null && key.equals(p.getKey()))
        .map(Person::getKey)
        .findFirst()
        .orElse(null);
  }

  private String findCyberAccusedPersonRef(List<Person> persons, String victimRef) {
    return persons.stream()
        .filter(p -> p != null && p.getKey() != null && !p.getKey().equals(victimRef))
        .filter(p -> !Ech051Constants.INSURER_REF_CYBER.equals(p.getKey()))
        .filter(this::isCyberAccusedCounterparty)
        .map(Person::getKey)
        .findFirst()
        .orElse(null);
  }

  /**
   * Contrepartie cybercrime : personne physique avec catégorie d'identité "U" (inconnue / traces numériques)
   * ou personne morale (vendeur entreprise), hors assureur placeholder.
   */
  private boolean isCyberAccusedCounterparty(Person p) {
    if (p.getNaturalIdentity() != null
        && Ech051Constants.IDENTITY_CATEGORY_UNKNOWN.equals(p.getNaturalIdentity().getIdentityCategory())) {
      return true;
    }
    if (p.getType() == PersonType.LEGAL && p.getLegalIdentity() != null) {
      String name = p.getLegalIdentity().getCurrentName();
      return name != null && !name.isBlank() && !"aucune".equalsIgnoreCase(name.strip());
    }
    return false;
  }

  private void buildCyberVictimAccusedPersonLink(String victimRef, String accusedRef, Relations.RelationsBuilder builder) {
    if (victimRef == null || accusedRef == null) {
      return;
    }
    builder.personLink(
        PersonLink.builder()
            .person1Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person1Ref(victimRef)
            .person2Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_ACCUSED_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_ACCUSED_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person2Ref(accusedRef)
            .build()
    );
  }

  private ObjectItem findObjectByKey(List<ObjectItem> objects, String key) {
    return objects.stream()
        .filter(o -> o != null && key.equals(o.getKey()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Construit les relations pour une déclaration individuelle.
   */
  private void buildIndividualRelations(List<Person> persons, String eventRef, String businessCaseRef,
                                        List<ObjectItem> objects, List<VehicleItem> vehicles,
                                        Relations.RelationsBuilder builder) {
    String victimRef = persons.stream().findFirst().map(Person::getKey).orElse(null);

    buildInvolvedPartyVictim(victimRef, eventRef, businessCaseRef, builder);
    buildObjectRelationsIndividual(objects, eventRef, victimRef, builder);
    buildVehicleRelationsIndividual(vehicles, eventRef, victimRef, builder);
  }

  /**
   * Construit les relations pour une déclaration tiers.
   */
  private void buildTiersRelations(List<Person> persons, String eventRef, String businessCaseRef,
                                   List<ObjectItem> objects, List<VehicleItem> vehicles,
                                   Relations.RelationsBuilder builder) {
    String tiersRef = !persons.isEmpty() ? persons.get(0).getKey() : null;
    String declarantRef = persons.size() > 1 ? persons.get(1).getKey() : null;

    buildPersonLink(declarantRef, tiersRef, builder);
    buildInvolvedPartyVictim(tiersRef, eventRef, businessCaseRef, builder);
    buildInvolvedPartyRepresentative(declarantRef, eventRef, businessCaseRef, builder);
    buildObjectRelationsTiers(objects, eventRef, tiersRef, declarantRef, builder);
    buildVehicleRelationsTiers(vehicles, eventRef, tiersRef, declarantRef, builder);
  }

  /**
   * Construit les relations pour une déclaration entreprise.
   * Ordre selon declarer-pour-une-entreprise.xml :
   * - personLink : déclarant (6, représentant) <-> organisation (4, lésé)
   * - personLink : informant (8, pers. donnant des rens.) <-> organisation (4, lésé)
   * - involvedParty : organisation (4) = lésé
   * - involvedParty : déclarant (6) = représentant
   * - involvedParty : informant (8) = pers. donnant des rens.
   * - objectPersonLink avec insurerRef=3
   */
  private void buildEntrepriseRelations(List<Person> persons, String eventRef, String businessCaseRef,
                                        List<ObjectItem> objects, List<VehicleItem> vehicles,
                                        Relations.RelationsBuilder builder) {
    String organisationRef = !persons.isEmpty() ? persons.get(0).getKey() : null;
    String declarantRef = persons.size() > 1 ? persons.get(1).getKey() : null;
    String informantRef = persons.size() > THIRD_PERSON_INDEX ? persons.get(THIRD_PERSON_INDEX).getKey() : null;

    buildPersonLink(declarantRef, organisationRef, builder);
    buildPersonLinkInformant(informantRef, organisationRef, builder);

    buildInvolvedPartyVictim(organisationRef, eventRef, businessCaseRef, builder);
    buildInvolvedPartyRepresentative(declarantRef, eventRef, businessCaseRef, builder);
    buildInvolvedPartyInformant(informantRef, eventRef, businessCaseRef, builder);

    buildObjectRelationsEntreprise(objects, eventRef, declarantRef, builder);
    buildVehicleRelationsEntreprise(vehicles, eventRef, organisationRef, builder);
  }

  /**
   * Construit le lien entre l'informant et le lésé.
   */
  private void buildPersonLinkInformant(String informantRef, String leseRef, Relations.RelationsBuilder builder) {
    if (informantRef == null || leseRef == null) {
      return;
    }
    builder.personLink(
        PersonLink.builder()
            .person1Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_INFORMANT_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_INFORMANT_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person1Ref(informantRef)
            .person2Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person2Ref(leseRef)
            .build()
    );
  }

  /**
   * Construit l'involvedParty pour l'informant.
   */
  private void buildInvolvedPartyInformant(String personRef, String eventRef, String businessCaseRef,
                                           Relations.RelationsBuilder builder) {
    if (personRef == null || eventRef == null || businessCaseRef == null) {
      return;
    }
    builder.involvedParty(
        InvolvedParty.builder()
            .businessCaseRef(businessCaseRef)
            .typeOfInvolvement(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_INFORMANT_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_INFORMANT_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .personRef(personRef)
            .eventRef(eventRef)
            .build()
    );
  }

  /**
   * Construit les relations objets pour une déclaration entreprise.
   */
  private void buildObjectRelationsEntreprise(List<ObjectItem> objects, String eventRef, String declarantRef,
                                              Relations.RelationsBuilder builder) {
    for (ObjectItem object : objects) {
      buildEventObjectLink(object, eventRef, builder);

      if (declarantRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .insurerRef(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE)
                .personRef(declarantRef)
                .build()
        );
      }
    }
  }

  /**
   * Construit les relations véhicules pour une déclaration entreprise.
   */
  private void buildVehicleRelationsEntreprise(List<VehicleItem> vehicles, String eventRef,
                                               String organisationRef,
                                               Relations.RelationsBuilder builder) {
    for (VehicleItem vehicle : vehicles) {
      buildEventVehicleLink(vehicle, eventRef, builder);

      if (organisationRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE)
                .personRef(organisationRef)
                .build()
        );
      }
    }
  }

  /**
   * Construit le lien entre les deux personnes (représentant <-> lésé).
   */
  private void buildPersonLink(String declarantRef, String tiersRef, Relations.RelationsBuilder builder) {
    if (declarantRef == null || tiersRef == null) {
      return;
    }
    builder.personLink(
        PersonLink.builder()
            .person1Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person1Ref(declarantRef)
            .person2Role(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .person2Ref(tiersRef)
            .build()
    );
  }

  /**
   * Construit l'involvedParty pour le lésé (victime).
   */
  private void buildInvolvedPartyVictim(String personRef, String eventRef, String businessCaseRef,
                                        Relations.RelationsBuilder builder) {
    if (personRef == null || eventRef == null || businessCaseRef == null) {
      return;
    }
    builder.involvedParty(
        InvolvedParty.builder()
            .businessCaseRef(businessCaseRef)
            .typeOfInvolvement(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .personRef(personRef)
            .eventRef(eventRef)
            .build()
    );
  }

  /**
   * Construit l'involvedParty pour le représentant.
   */
  private void buildInvolvedPartyRepresentative(String personRef, String eventRef, String businessCaseRef,
                                                Relations.RelationsBuilder builder) {
    if (personRef == null || eventRef == null || businessCaseRef == null) {
      return;
    }
    builder.involvedParty(
        InvolvedParty.builder()
            .businessCaseRef(businessCaseRef)
            .typeOfInvolvement(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .personRef(personRef)
            .eventRef(eventRef)
            .build()
    );
  }

  private void buildInvolvedPartyAccused(String personRef, String eventRef, String businessCaseRef,
                                         Relations.RelationsBuilder builder) {
    if (personRef == null || eventRef == null || businessCaseRef == null) {
      return;
    }
    builder.involvedParty(
        InvolvedParty.builder()
            .businessCaseRef(businessCaseRef)
            .typeOfInvolvement(RipolReferenceBuilder.of(
                Ech051Constants.INVOLVEMENT_TYPE_ACCUSED_CODE,
                Ech051Constants.INVOLVEMENT_TYPE_ACCUSED_LABEL,
                Ech051Constants.INVOLVEMENT_SOURCE_TABLE
            ))
            .personRef(personRef)
            .eventRef(eventRef)
            .build()
    );
  }

  /**
   * Construit le lien entre l'événement et le dossier.
   */
  private void buildEventBusinessCaseLink(String eventRef, String businessCaseRef,
                                          Relations.RelationsBuilder builder) {
    if (eventRef == null || businessCaseRef == null) {
      return;
    }
    builder.eventBusinessCaseLink(
        EventBusinessCaseLink.builder()
            .eventRef(eventRef)
            .businessCaseRef(businessCaseRef)
            .build()
    );
  }

  /**
   * Construit les relations objets pour une déclaration individuelle.
   */
  private void buildObjectRelationsIndividual(List<ObjectItem> objects, String eventRef, String victimRef,
                                              Relations.RelationsBuilder builder) {
    for (ObjectItem object : objects) {
      buildEventObjectLink(object, eventRef, builder);

      if (victimRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .personRef(victimRef)
                .build()
        );
      }
    }
  }

  /**
   * Construit les relations objets pour une déclaration tiers.
   */
  private void buildObjectRelationsTiers(List<ObjectItem> objects, String eventRef,
                                         String tiersRef, String declarantRef,
                                         Relations.RelationsBuilder builder) {
    for (ObjectItem object : objects) {
      buildEventObjectLink(object, eventRef, builder);

      if (tiersRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(tiersRef)
                .build()
        );
      }

      if (declarantRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(declarantRef)
                .build()
        );
      }

      if (tiersRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(tiersRef)
                .build()
        );
      }

      if (declarantRef != null) {
        builder.objectPersonLink(
            ObjectPersonLink.builder()
                .objectRef(object.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(declarantRef)
                .build()
        );
      }
    }
  }

  /**
   * Construit le lien eventObjectLink.
   */
  private void buildEventObjectLink(ObjectItem object, String eventRef, Relations.RelationsBuilder builder) {
    if (eventRef == null) {
      return;
    }
    builder.eventObjectLink(
        EventObjectLink.builder()
            .eventRef(eventRef)
            .objectRole(RipolReferenceBuilder.of(
                Ech051Constants.OBJECT_ROLE_SEARCHED_CODE,
                Ech051Constants.OBJECT_ROLE_SEARCHED_LABEL,
                Ech051Constants.OBJECT_ROLE_SOURCE_TABLE
            ))
            .objectRef(object.getKey())
            .build()
    );
  }

  /**
   * Construit les relations véhicules pour une déclaration individuelle.
   */
  private void buildVehicleRelationsIndividual(List<VehicleItem> vehicles, String eventRef, String victimRef,
                                               Relations.RelationsBuilder builder) {
    for (VehicleItem vehicle : vehicles) {
      buildEventVehicleLink(vehicle, eventRef, builder);

      if (victimRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .personRef(victimRef)
                .insurerRef(Ech051Constants.INSURER_REF_VEHICLE)
                .build()
        );
      }
    }
  }

  /**
   * Construit les relations véhicules pour une déclaration tiers.
   */
  private void buildVehicleRelationsTiers(List<VehicleItem> vehicles, String eventRef,
                                          String tiersRef, String declarantRef,
                                          Relations.RelationsBuilder builder) {
    for (VehicleItem vehicle : vehicles) {
      buildEventVehicleLink(vehicle, eventRef, builder);

      if (tiersRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(tiersRef)
                .build()
        );
      }

      if (declarantRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(declarantRef)
                .build()
        );
      }

      if (tiersRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_VICTIM_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(tiersRef)
                .build()
        );
      }

      if (declarantRef != null) {
        builder.vehiclePersonLink(
            VehiclePersonLink.builder()
                .vehicleRef(vehicle.getKey())
                .personRole(RipolReferenceBuilder.of(
                    Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_CODE,
                    Ech051Constants.INVOLVEMENT_TYPE_REPRESENTATIVE_LABEL,
                    Ech051Constants.INVOLVEMENT_SOURCE_TABLE
                ))
                .insurerRef(Ech051Constants.INSURER_REF)
                .personRef(declarantRef)
                .build()
        );
      }
    }
  }

  /**
   * Construit le lien eventVehicleLink.
   */
  private void buildEventVehicleLink(VehicleItem vehicle, String eventRef, Relations.RelationsBuilder builder) {
    if (eventRef == null) {
      return;
    }
    builder.eventVehicleLink(
        EventVehicleLink.builder()
            .eventRef(eventRef)
            .vehicleRole(RipolReferenceBuilder.of(
                Ech051Constants.VEHICLE_ROLE_SEARCHED_CODE,
                Ech051Constants.VEHICLE_ROLE_SEARCHED_LABEL,
                Ech051Constants.VEHICLE_ROLE_SOURCE_TABLE
            ))
            .vehicleRef(vehicle.getKey())
            .build()
    );
  }

  private boolean isCyberCommandeFrauduleuse(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return cybercrime.getTypeCybercrime() == TypeCybercrime.COMMANDE_FRAUDULEUSE
        || cybercrime.getCommandeFrauduleuse() != null;
  }

  private boolean isCyberTransactionType(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return isCyberCommandeFrauduleuse(cybercrime)
        || cybercrime.getTypeCybercrime() == TypeCybercrime.ACHAT_NON_RECU
        || cybercrime.getAchatNonRecu() != null
        || cybercrime.getTypeCybercrime() == TypeCybercrime.FAUSSE_ANNONCE
        || cybercrime.getFausseAnnonce() != null;
  }

  /** IBAN saisi avec espaces : compacté pour l’élément {@code accountReceive} SEP. */
  private static String normalizeIbanForSep(String iban) {
    if (iban == null || iban.isBlank()) {
      return null;
    }
    return iban.strip().replace(" ", "").replace("\u00a0", "");
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.strip();
  }

}
