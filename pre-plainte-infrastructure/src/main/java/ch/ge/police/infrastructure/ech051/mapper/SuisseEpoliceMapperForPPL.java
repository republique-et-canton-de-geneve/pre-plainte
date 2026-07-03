package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.PlateformeUtilisee;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Communication;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.LegalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NaturalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Relations;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Mapper principal pour transformer une {@link PrePlainte} vers le format eCH-0051
 * utilisé par le système Suisse ePolice (SEP).
 *
 * <p>Ce mapper agit comme orchestrateur en déléguant la construction de chaque
 * section du document eCH-0051 à des mappers spécialisés :</p>
 * <ul>
 *   <li>{@link SuisseEpolicePersonMapper} - Personnes physiques et morales</li>
 *   <li>{@link SuisseEpoliceEventMapper} - Événements et incidents</li>
 *   <li>{@link SuisseEpoliceObjectMapper} - Objets volés et véhicules</li>
 *   <li>{@link SuisseEpoliceRelationsMapper} - Relations entre entités</li>
 *   <li>{@link SuisseEpoliceBusinessCaseMapper} - Dossiers et pièces jointes</li>
 * </ul>
 *
 * <p>Cette architecture modulaire améliore la lisibilité, la maintenabilité
 * et facilite les tests unitaires de chaque composant.</p>
 */
@Component
@RequiredArgsConstructor
public class SuisseEpoliceMapperForPPL {

  private final SuisseEpolicePersonMapper personMapper;
  private final SuisseEpoliceEventMapper eventMapper;
  private final SuisseEpoliceObjectMapper objectMapper;
  private final SuisseEpoliceRelationsMapper relationsMapper;
  private final SuisseEpoliceBusinessCaseMapper businessCaseMapper;
  private final SuisseEpoliceAddressMapper suisseEpoliceAddressMapper;

  /**
   * Transforme une pré-plainte en document eCH-0051 complet.
   *
   * @param prePlainte la pré-plainte à transformer
   * @return le document eCH-0051 structuré, ou null si la pré-plainte est null
   */
  public Ech0051DocumentPayload toDocument(PrePlainte prePlainte) {
    if (prePlainte == null) {
      return null;
    }

    InformationsPersonnelles infos = prePlainte.getInformationsPersonnelles();
    IncidentBase incident = prePlainte.getIncident() != null
        ? prePlainte.getIncident().getDetails()
        : null;

    DeclarationType declarationType = resolveDeclarationType(infos);

    List<ObjectItem> objects = objectMapper.buildObjectsFromIncident(incident);
    if (incident instanceof Cybercrime cybercrimeForObjects) {
      objects = mergeCyberObjects(objects, objectMapper.buildCyberObjects(cybercrimeForObjects, infos));
    }
    List<VehicleItem> vehicles = objectMapper.buildVehiclesFromIncident(incident);
    boolean hasVehicles = !vehicles.isEmpty();
    String vehicleInsurerName = resolveVehicleInsurerName(incident);

    List<Person> persons = personMapper.buildPersons(infos, hasVehicles, vehicleInsurerName);
    ensureCyberCommandeFrauduleusePersonRefs(persons, incident);
    persons = applyCyberVictimAttributes(persons, incident);
    persons = addCyberCounterpartyIfPresent(persons, incident);
    persons = addCyberPaymentBeneficiaryIfPresent(persons, incident);
    List<Event> events = incident == null
        ? List.of()
        : eventMapper.buildEvents(incident, infos);
    BusinessCase businessCase = businessCaseMapper.buildBusinessCase(prePlainte, declarationType, hasVehicles);

    Relations relations = relationsMapper.buildRelations(
        persons, events, objects, vehicles, businessCase, declarationType, incident
    );

    return Ech0051DocumentPayload.builder()
        .processData(eventMapper.buildProcessData(incident, prePlainte.getDemandeId()))
        .persons(persons)
        .objects(objects)
        .vehicles(vehicles)
        .events(events)
        .businessCase(businessCase)
        .relations(relations)
        .build();
  }

  private boolean isCyberTransactionType(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    TypeCybercrime type = cybercrime.getTypeCybercrime();
    return type == TypeCybercrime.COMMANDE_FRAUDULEUSE
        || type == TypeCybercrime.ACHAT_NON_RECU
        || type == TypeCybercrime.FAUSSE_ANNONCE;
  }

  private void ensureCyberCommandeFrauduleusePersonRefs(List<Person> persons, IncidentBase incident) {
    if (!(incident instanceof Cybercrime cybercrime)
        || !isCyberTransactionType(cybercrime)) {
      return;
    }

    boolean hasCyberInsurer = persons.stream().anyMatch(p -> p != null && Ech051Constants.INSURER_REF_CYBER.equals(p.getKey()));
    if (!hasCyberInsurer) {
      persons.add(personMapper.buildInsurancePerson(Ech051Constants.INSURER_REF_CYBER, Ech051Constants.INSURER_NAME_NONE));
    }
  }

  private List<Person> addCyberCounterpartyIfPresent(List<Person> persons, IncidentBase incident) {
    if (!(incident instanceof Cybercrime cybercrime) || !shouldAddCyberCounterparty(cybercrime)) {
      return persons;
    }

    CyberCounterpartyData counterpartyData = extractCyberCounterpartyData(cybercrime);
    if (counterpartyData.isEmpty()) {
      return persons;
    }

    List<Person> updatedPersons = new ArrayList<>(persons);
    String personKey = String.valueOf(nextNumericKey(updatedPersons));

    Person counterparty = buildCyberCounterparty(counterpartyData, updatedPersons, personKey);
    updatedPersons.add(counterparty);
    return updatedPersons;
  }

  private Person buildCyberCounterparty(
      CyberCounterpartyData counterpartyData,
      List<Person> updatedPersons,
      String personKey
  ) {
    Communication communication = Communication.builder()
        .email(counterpartyData.email())
        .phone(counterpartyData.phone())
        .uri(counterpartyData.uri())
        .uriProvider(counterpartyData.uriProvider())
        .build();

    List<Address> addresses = buildCounterpartyAddresses(counterpartyData);

    if (counterpartyData.legalEntity()) {
      return buildLegalCounterparty(counterpartyData, personKey, communication, addresses);
    }

    String identityKey = String.valueOf(nextIdentityNumericKey(updatedPersons));
    return buildNaturalCounterparty(counterpartyData, personKey, identityKey, communication, addresses);
  }

  private Person buildLegalCounterparty(
      CyberCounterpartyData counterpartyData,
      String personKey,
      Communication communication,
      List<Address> addresses
  ) {
    String companyName = resolveLegalCounterpartyName(counterpartyData.legalCurrentName());
    return Person.builder()
        .key(personKey)
        .type(Ech0051DocumentPayload.PersonType.LEGAL)
        .legalIdentity(LegalIdentity.builder()
            .currentName(companyName)
            .build())
        .communication(isBlankCommunication(communication) ? null : communication)
        .addresses(addresses)
        .address(addresses.isEmpty() ? null : addresses.getFirst())
        .additionalInformation(counterpartyData.personAdditionalInformation())
        .build();
  }

  private Person buildNaturalCounterparty(
      CyberCounterpartyData counterpartyData,
      String personKey,
      String identityKey,
      Communication communication,
      List<Address> addresses
  ) {
    NaturalIdentity identity = NaturalIdentity.builder()
        .key(identityKey)
        .identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN)
        .additionalInformation(counterpartyData.identityAdditionalInformation())
        .officialName(nullToUnknown(counterpartyData.officialName()))
        .firstName(nullToUnknown(counterpartyData.firstName()))
        .build();

    return Person.builder()
        .key(personKey)
        .type(Ech0051DocumentPayload.PersonType.NATURAL)
        .naturalIdentity(identity)
        .communication(isBlankCommunication(communication) ? null : communication)
        .addresses(addresses)
        .address(addresses.isEmpty() ? null : addresses.getFirst())
        .additionalInformation(counterpartyData.personAdditionalInformation())
        .build();
  }

  private String resolveLegalCounterpartyName(String companyName) {
    if (companyName == null || companyName.isBlank()) {
      return "Entreprise (identité partielle)";
    }
    return companyName.strip();
  }

  private static boolean shouldAddCyberCounterparty(Cybercrime cybercrime) {
    TypeCybercrime type = cybercrime.getTypeCybercrime();
    return type == TypeCybercrime.COMMANDE_FRAUDULEUSE
        || type == TypeCybercrime.ACHAT_NON_RECU
        || type == TypeCybercrime.FAUSSE_ANNONCE;
  }

  private static String nullToUnknown(String value) {
    return (value == null || value.isBlank()) ? "Inconnu" : value;
  }

  private CyberCounterpartyData extractCyberCounterpartyData(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return null;
    }

    AchatNonRecu achat = cybercrime.getAchatNonRecu();
    if (achat != null) {
      String personCtx = buildAchatCounterpartyPersonAdditionalInformation(cybercrime, achat);
      String uri;
      String uriProvider;
      if (Boolean.TRUE.equals(achat.getAchatViaPlaceMarche())) {
        uri = resolveMarketplaceUri(achat);
        uriProvider = resolveMarketplaceUriProvider(achat);
      } else if (isNotBlank(achat.getSiteWebEntrepriseVendeur())) {
        uri = trimToNull(achat.getSiteWebEntrepriseVendeur());
        uriProvider = Ech051Constants.URI_PROVIDER_ENTREPRISE_VENDEUR;
      } else {
        uri = null;
        uriProvider = null;
      }
      return new CyberCounterpartyData(
          achat.getNomVendeur(),
          achat.getPrenomVendeur(),
          achat.getEmailVendeur(),
          achat.getTelephoneVendeur(),
          uri,
          uriProvider,
          achat.getAdresseVendeur(),
          null,
          personCtx,
          null,
          false,
          null
      );
    }

    FausseAnnonce fausseAnnonce = cybercrime.getFausseAnnonce();
    if (fausseAnnonce != null) {
      return new CyberCounterpartyData(
          fausseAnnonce.getNomBailleur(),
          null,
          fausseAnnonce.getEmailBailleur(),
          fausseAnnonce.getTelephoneBailleur(),
          null,
          null,
          null,
          null,
          buildFausseAnnonceCounterpartyPersonAdditionalInformation(cybercrime, fausseAnnonce),
          null,
          false,
          null
      );
    }

    CommandeFrauduleuse commandeFrauduleuse = cybercrime.getCommandeFrauduleuse();
    if (commandeFrauduleuse != null) {
      String foreignAddressLine = Boolean.FALSE.equals(commandeFrauduleuse.getLivraisonAdresseLesee())
          ? formatForeignAddressLine(commandeFrauduleuse.getAdresseLivraison())
          : null;
      return new CyberCounterpartyData(
          commandeFrauduleuse.getNomContrevenant(),
          commandeFrauduleuse.getPrenomContrevenant(),
          commandeFrauduleuse.getEmailCommande(),
          commandeFrauduleuse.getTelephoneCommande(),
          trimToNull(commandeFrauduleuse.getSiteWebContrevenant()),
          null,
          commandeFrauduleuse.getAdresseContrevenant(),
          foreignAddressLine,
          trimToNull(commandeFrauduleuse.getPrestataire()),
          null,
          false,
          null
      );
    }

    return new CyberCounterpartyData(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null
    );
  }

  private List<Person> applyCyberVictimAttributes(List<Person> persons, IncidentBase incident) {
    if (!(incident instanceof Cybercrime cybercrime)) {
      return persons;
    }
    List<Person> updated = new ArrayList<>(persons.size());
    for (Person person : persons) {
      if (person == null || !Ech051Constants.PERSON_KEY_TIERS.equals(person.getKey())) {
        updated.add(person);
        continue;
      }
      Person enriched = person;
      if (cybercrime.getAchatNonRecu() != null) {
        enriched = applyAchatVictimAttributes(enriched, cybercrime.getAchatNonRecu());
      }
      if (cybercrime.getCommandeFrauduleuse() != null) {
        enriched = applyCommandeFrauduleuseVictimAttributes(enriched, cybercrime.getCommandeFrauduleuse());
      }
      updated.add(enriched);
    }
    return updated;
  }

  private Person applyAchatVictimAttributes(Person victim, AchatNonRecu achat) {
    Person.PersonBuilder builder = victim.toBuilder();
    if (achat.getAchatViaPlaceMarche() != null) {
      builder.onlineShop(achat.getAchatViaPlaceMarche());
    }
    if (Boolean.TRUE.equals(achat.getPreuvePaiementIndisponible())) {
      builder.noPaymentProofReason(trimToNull(achat.getRaisonAbsencePreuvePaiement()));
    }
    if (Boolean.TRUE.equals(achat.getAnnonceDocumentIndisponible())) {
      builder.noAdImageReason(trimToNull(achat.getRaisonAbsenceAnnonce()));
    }
    if (achat.getCopieIdentiteTransmiseAuteur() != null) {
      builder.reporterIdCopySent(achat.getCopieIdentiteTransmiseAuteur());
    }
    if (Boolean.TRUE.equals(achat.getCopieIdentiteTransmiseAuteurDocumentIndisponible())) {
      builder.noIdCopyPresentReason(trimToNull(achat.getRaisonAbsenceCopieIdentiteTransmiseAuteur()));
    }
    if (achat.getCopieIdentiteAuteurTransmise() != null) {
      builder.perpetratorIdCopyReceived(achat.getCopieIdentiteAuteurTransmise());
    }
    if (Boolean.TRUE.equals(achat.getCopieIdentiteAuteurDocumentIndisponible())) {
      builder.noPerpetratorsIdCopyPresentReason(trimToNull(achat.getRaisonAbsenceCopieIdentiteAuteur()));
    }
    return builder.build();
  }

  private Person applyCommandeFrauduleuseVictimAttributes(Person victim, CommandeFrauduleuse commande) {
    Person.PersonBuilder builder = victim.toBuilder();
    if (commande.getLivraisonAdresseLesee() != null) {
      builder.deliveredAbroad(!commande.getLivraisonAdresseLesee());
    }
    if (commande.getMoyenPaiementNumeriqueDebite() != null) {
      builder.creditCardUsed(commande.getMoyenPaiementNumeriqueDebite());
    }
    if (commande.getCopieIdentiteTransmiseAuteur() != null) {
      builder.reporterIdCopySent(commande.getCopieIdentiteTransmiseAuteur());
    }
    if (Boolean.TRUE.equals(commande.getCopieIdentiteTransmiseAuteurDocumentIndisponible())) {
      builder.noIdCopyPresentReason(trimToNull(commande.getRaisonAbsenceCopieIdentiteTransmiseAuteur()));
    }
    if (commande.getCopieIdentiteAuteurTransmise() != null) {
      builder.perpetratorIdCopyReceived(commande.getCopieIdentiteAuteurTransmise());
    }
    if (Boolean.TRUE.equals(commande.getCopieIdentiteAuteurDocumentIndisponible())) {
      builder.noPerpetratorsIdCopyPresentReason(trimToNull(commande.getRaisonAbsenceCopieIdentiteAuteur()));
    }
    return builder.build();
  }

  private List<Person> addCyberPaymentBeneficiaryIfPresent(List<Person> persons, IncidentBase incident) {
    if (!(incident instanceof Cybercrime cybercrime) || cybercrime.getAchatNonRecu() == null) {
      return persons;
    }
    AchatNonRecu achat = cybercrime.getAchatNonRecu();
    if (!hasPaymentBeneficiaryData(achat)) {
      return persons;
    }

    List<Person> updatedPersons = new ArrayList<>(persons);
    String personKey = String.valueOf(nextNumericKey(updatedPersons));
    String identityKey = String.valueOf(nextIdentityNumericKey(updatedPersons));

    String remark = null;
    if (achat.getSocieteBeneficiaire() != null && !achat.getSocieteBeneficiaire().isBlank()) {
      remark = "Société du bénéficiaire du paiement: " + achat.getSocieteBeneficiaire().strip();
    }

    NaturalIdentity identity = NaturalIdentity.builder()
        .key(identityKey)
        .identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN)
        .officialName(nullToUnknown(achat.getNomBeneficiaire()))
        .firstName(nullToUnknown(achat.getPrenomBeneficiaire()))
        .build();

    Person beneficiary = Person.builder()
        .key(personKey)
        .type(Ech0051DocumentPayload.PersonType.NATURAL)
        .naturalIdentity(identity)
        .remark(remark)
        .build();

    updatedPersons.add(beneficiary);
    return updatedPersons;
  }

  private static boolean hasPaymentBeneficiaryData(AchatNonRecu achat) {
    return isNotBlank(achat.getNomBeneficiaire())
        || isNotBlank(achat.getPrenomBeneficiaire())
        || isNotBlank(achat.getSocieteBeneficiaire());
  }

  private static List<ObjectItem> mergeCyberObjects(List<ObjectItem> baseObjects, List<ObjectItem> cyberObjects) {
    if (cyberObjects == null || cyberObjects.isEmpty()) {
      return baseObjects;
    }
    if (baseObjects == null || baseObjects.isEmpty()) {
      return cyberObjects;
    }
    List<ObjectItem> merged = new ArrayList<>(baseObjects);
    merged.addAll(cyberObjects);
    return merged;
  }

  private static String resolveMarketplaceUri(AchatNonRecu achat) {
    if (achat == null || !Boolean.TRUE.equals(achat.getAchatViaPlaceMarche())) {
      return null;
    }
    return trimToNull(achat.getPlateformeId());
  }

  private static String resolveMarketplaceUriProvider(AchatNonRecu achat) {
    if (achat == null || !Boolean.TRUE.equals(achat.getAchatViaPlaceMarche())) {
      return null;
    }
    PlateformeUtilisee plateforme = achat.getPlateformeUtilisee();
    if (plateforme == null) {
      return null;
    }
    if (plateforme == PlateformeUtilisee.AUTRE) {
      return trimToNull(achat.getPlateformeAutre());
    }
    return plateforme.getLabel();
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.strip();
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }

  private String buildAchatCounterpartyPersonAdditionalInformation(Cybercrime cybercrime, AchatNonRecu achat) {
    if (cybercrime == null || achat == null) {
      return null;
    }
    StringJoiner j = new StringJoiner(" | ");
    if (Boolean.FALSE.equals(achat.getAchatViaPlaceMarche())) {
      appendIfNotBlank(j, "Nom entreprise vendeur", achat.getNomEntrepriseVendeur());
    }
    appendBooleanAsOuiNon(j, "E-mail vendeur inconnu", achat.getEmailVendeurInconnu());
    String s = j.toString();
    return s.isBlank() ? null : s;
  }

  private String buildFausseAnnonceCounterpartyPersonAdditionalInformation(
      Cybercrime cybercrime,
      FausseAnnonce fausseAnnonce
  ) {
    if (cybercrime == null || fausseAnnonce == null) {
      return null;
    }
    StringJoiner j = new StringJoiner(" | ");
    appendIfNotBlank(j, "Titre annonce", fausseAnnonce.getTitreAnnonce());
    if (fausseAnnonce.getMontantDemande() != null) {
      appendIfNotBlank(j, "Montant demandé", fausseAnnonce.getMontantDemande().toString());
    }
    appendIfNotBlank(j, "Mode de paiement demandé", fausseAnnonce.getModePaiementDemande());
    String s = j.toString();
    return s.isBlank() ? null : s;
  }

  private static void appendBooleanAsOuiNon(StringJoiner joiner, String label, Boolean value) {
    if (value == null || joiner == null) {
      return;
    }
    joiner.add(label + ": " + (value ? "oui" : "non"));
  }

  private static void appendIfNotBlank(StringJoiner joiner, String label, String value) {
    if (value != null && !value.isBlank()) {
      joiner.add(label + ": " + value.strip());
    }
  }

  private Address buildCounterpartyPayloadAddress(Adresse domainAdresse) {
    if (domainAdresse == null || isCounterpartyAdresseEmpty(domainAdresse)) {
      return null;
    }
    return suisseEpoliceAddressMapper.fromAdresse(domainAdresse);
  }

  private List<Address> buildCounterpartyAddresses(CyberCounterpartyData counterpartyData) {
    List<Address> addresses = new ArrayList<>();
    if (counterpartyData.foreignAddressLine() != null && !counterpartyData.foreignAddressLine().isBlank()) {
      addresses.add(Address.builder().addressLine(counterpartyData.foreignAddressLine().strip()).build());
    }
    Address structured = buildCounterpartyPayloadAddress(counterpartyData.structuredAddress());
    if (structured != null) {
      addresses.add(structured);
    }
    return addresses;
  }

  private static String formatForeignAddressLine(Adresse adresse) {
    if (adresse == null || isCounterpartyAdresseEmpty(adresse)) {
      return null;
    }
    StringJoiner joiner = new StringJoiner(", ");
    appendAddressPart(joiner, adresse.adresse());
    String npaLocalite = ((adresse.npa() != null ? adresse.npa().strip() : "")
        + " "
        + (adresse.localite() != null ? adresse.localite().strip() : "")).strip();
    appendAddressPart(joiner, npaLocalite.isBlank() ? null : npaLocalite);
    appendAddressPart(joiner, adresse.pays());
    String formatted = joiner.toString();
    return formatted.isBlank() ? null : formatted;
  }

  private static void appendAddressPart(StringJoiner joiner, String value) {
    if (value != null && !value.isBlank()) {
      joiner.add(value.strip());
    }
  }

  private static boolean isCounterpartyAdresseEmpty(Adresse a) {
    if (a == null) {
      return true;
    }
    return (a.adresse() == null || a.adresse().isBlank())
        && (a.adressePostale() == null || a.adressePostale().isBlank())
        && (a.npa() == null || a.npa().isBlank())
        && (a.localite() == null || a.localite().isBlank())
        && (a.localiteCode() == null || a.localiteCode().isBlank())
        && (a.pays() == null || a.pays().isBlank())
        && (a.paysCode() == null || a.paysCode().isBlank());
  }

  private int nextNumericKey(List<Person> persons) {
    return persons.stream()
        .map(Person::getKey)
        .map(this::parseIntOrZero)
        .max(Integer::compareTo)
        .orElse(0) + 1;
  }

  private int nextIdentityNumericKey(List<Person> persons) {
    return persons.stream()
        .map(Person::getNaturalIdentity)
        .filter(identity -> identity != null && identity.getKey() != null)
        .map(NaturalIdentity::getKey)
        .map(this::parseIntOrZero)
        .max(Integer::compareTo)
        .orElse(0) + 1;
  }

  private int parseIntOrZero(String value) {
    if (value == null) {
      return 0;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private boolean isBlankCommunication(Communication communication) {
    return communication == null
        || ((communication.getEmail() == null || communication.getEmail().isBlank())
        && (communication.getPhone() == null || communication.getPhone().isBlank())
        && (communication.getMobile() == null || communication.getMobile().isBlank())
        && (communication.getUri() == null || communication.getUri().isBlank())
        && (communication.getUriProvider() == null || communication.getUriProvider().isBlank()));
  }

  private record CyberCounterpartyData(
      String officialName,
      String firstName,
      String email,
      String phone,
      String uri,
      String uriProvider,
      Adresse structuredAddress,
      String foreignAddressLine,
      String personAdditionalInformation,
      String identityAdditionalInformation,
      boolean legalEntity,
      String legalCurrentName
  ) {
    private boolean isEmpty() {
      if (legalEntity) {
        return isBlank(legalCurrentName)
            && isBlank(email)
            && isBlank(phone)
            && isBlank(uri)
            && isBlank(uriProvider)
            && isCounterpartyAdresseEmpty(structuredAddress)
            && isBlank(foreignAddressLine)
            && isBlank(personAdditionalInformation);
      }
      return isBlank(officialName)
          && isBlank(firstName)
          && isBlank(email)
          && isBlank(phone)
          && isBlank(uri)
          && isBlank(uriProvider)
          && isCounterpartyAdresseEmpty(structuredAddress)
          && isBlank(foreignAddressLine)
          && isBlank(personAdditionalInformation)
          && isBlank(identityAdditionalInformation);
    }

    private boolean isBlank(String value) {
      return value == null || value.isBlank();
    }
  }

  private String resolveVehicleInsurerName(IncidentBase incident) {
    for (ObjetIncident objet : listVehicleObjets(incident)) {
      String nom = objet.resolveAssureurNom();
      if (nom != null && !nom.isBlank()) {
        return nom;
      }
    }
    return Ech051Constants.INSURER_NAME_NONE;
  }

  private List<ObjetIncident> listVehicleObjets(IncidentBase incident) {
    if (incident instanceof Vol vol && vol.getObjetsVoles() != null) {
      return vol.getObjetsVoles().stream().filter(ObjetIncident::isVehicleType).toList();
    }
    if (incident instanceof DommageMateriel dommage && dommage.getObjetDegrades() != null) {
      return dommage.getObjetDegrades().stream().filter(ObjetIncident::isVehicleType).toList();
    }
    return List.of();
  }

  /**
   * Détermine le type de déclaration selon les informations personnelles.
   */
  private DeclarationType resolveDeclarationType(InformationsPersonnelles infos) {
    if (infos == null) {
      return DeclarationType.INDIVIDUAL;
    }
    if (infos.hasOrganisation()) {
      return DeclarationType.ENTREPRISE;
    }
    if (infos.hasTiers()) {
      return DeclarationType.TIERS;
    }
    return DeclarationType.INDIVIDUAL;
  }
}
