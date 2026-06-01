package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
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
    List<VehicleItem> vehicles = objectMapper.buildVehiclesFromIncident(incident);
    boolean hasVehicles = !vehicles.isEmpty();

    List<Person> persons = personMapper.buildPersons(infos, hasVehicles);
    ensureCyberCommandeFrauduleusePersonRefs(persons, incident);
    persons = addCyberCounterpartyIfPresent(persons, incident);
    List<Event> events = incident == null
        ? List.of()
        : List.of(eventMapper.buildEvent(incident, infos));
    BusinessCase businessCase = businessCaseMapper.buildBusinessCase(prePlainte, declarationType);

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
      persons.add(personMapper.buildInsurancePerson(Ech051Constants.INSURER_REF_CYBER));
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

    Communication communication = Communication.builder()
        .email(counterpartyData.email())
        .phone(counterpartyData.phone())
        .uri(counterpartyData.uri())
        .build();

    Person counterparty;
    if (counterpartyData.legalEntity()) {
      String companyName = counterpartyData.legalCurrentName();
      if (companyName == null || companyName.isBlank()) {
        companyName = "Entreprise (identité partielle)";
      }
      counterparty = Person.builder()
          .key(personKey)
          .type(Ech0051DocumentPayload.PersonType.LEGAL)
          .legalIdentity(LegalIdentity.builder()
              .currentName(companyName.strip())
              .build())
          .communication(isBlankCommunication(communication) ? null : communication)
          .address(buildCounterpartyPayloadAddress(counterpartyData.address()))
          .additionalInformation(counterpartyData.personAdditionalInformation())
          .build();
    } else {
      String identityKey = String.valueOf(nextIdentityNumericKey(updatedPersons));
      NaturalIdentity identity = NaturalIdentity.builder()
          .key(identityKey)
          .identityCategory(Ech051Constants.IDENTITY_CATEGORY_UNKNOWN)
          .additionalInformation(counterpartyData.identityAdditionalInformation())
          .officialName(nullToUnknown(counterpartyData.officialName()))
          .firstName(nullToUnknown(counterpartyData.firstName()))
          .build();

      counterparty = Person.builder()
          .key(personKey)
          .type(Ech0051DocumentPayload.PersonType.NATURAL)
          .naturalIdentity(identity)
          .communication(isBlankCommunication(communication) ? null : communication)
          .address(buildCounterpartyPayloadAddress(counterpartyData.address()))
          .additionalInformation(counterpartyData.personAdditionalInformation())
          .build();
    }

    updatedPersons.add(counterparty);
    return updatedPersons;
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
      boolean entrepriseDirecte = Boolean.FALSE.equals(achat.getAchatViaPlaceMarche());
      String nomEntreprise = achat.getNomEntrepriseVendeur();
      if (entrepriseDirecte && nomEntreprise != null && !nomEntreprise.isBlank()) {
        return new CyberCounterpartyData(
            null,
            null,
            achat.getEmailVendeur(),
            achat.getTelephoneVendeur(),
            achat.getSiteWebEntrepriseVendeur(),
            achat.getAdresseVendeur(),
            personCtx,
            null,
            true,
            nomEntreprise.strip()
        );
      }
      return new CyberCounterpartyData(
          achat.getNomVendeur(),
          achat.getPrenomVendeur(),
          achat.getEmailVendeur(),
          achat.getTelephoneVendeur(),
          null,
          achat.getAdresseVendeur(),
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
          buildFausseAnnonceCounterpartyPersonAdditionalInformation(cybercrime, fausseAnnonce),
          null,
          false,
          null
      );
    }

    CommandeFrauduleuse commandeFrauduleuse = cybercrime.getCommandeFrauduleuse();
    if (commandeFrauduleuse != null) {
      return new CyberCounterpartyData(
          null,
          null,
          null,
          null,
          null,
          null,
          buildCommandeFrauduleuseCounterpartyPersonAdditionalInformation(cybercrime, commandeFrauduleuse),
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
        false,
        null
    );
  }

  private String buildAchatCounterpartyPersonAdditionalInformation(Cybercrime cybercrime, AchatNonRecu achat) {
    if (cybercrime == null || achat == null) {
      return null;
    }
    StringJoiner j = new StringJoiner(" | ");
    appendIfNotBlank(j, "ID plateforme", achat.getPlateformeId());
    if (achat.getPlateformeUtilisee() != null) {
      appendIfNotBlank(j, "Plateforme", achat.getPlateformeUtilisee().getLabel());
    }
    appendIfNotBlank(j, "Plateforme autre", achat.getPlateformeAutre());
    appendBooleanAsOuiNon(j, "Achat via place de marché", achat.getAchatViaPlaceMarche());
    appendIfNotBlank(j, "Nom entreprise vendeur", achat.getNomEntrepriseVendeur());
    appendIfNotBlank(j, "Site web entreprise vendeur", achat.getSiteWebEntrepriseVendeur());
    appendIfNotBlank(j, "Date opération", achat.getDateOperation());
    appendIfNotBlank(j, "Montant du délit", achat.getMontantDelitAchatLigne());
    appendIfNotBlank(j, "Article non livré", achat.getArticleNonLivreDescription());
    appendBooleanAsOuiNon(j, "E-mail vendeur inconnu", achat.getEmailVendeurInconnu());
    appendBooleanAsOuiNon(j, "Document annonce indisponible", achat.getAnnonceDocumentIndisponible());
    appendIfNotBlank(j, "Raison absence annonce", achat.getRaisonAbsenceAnnonce());
    if (achat.getMoyenPaiement() != null) {
      appendIfNotBlank(j, "Moyen de paiement", achat.getMoyenPaiement().getLabel());
    }
    appendIfNotBlank(j, "Moyen de paiement autre", achat.getMoyenPaiementAutre());
    appendIfNotBlank(j, "IBAN bénéficiaire", achat.getIbanBeneficiaire());
    appendIfNotBlank(j, "Compte PayPal bénéficiaire", achat.getComptePaypalBeneficiaire());
    appendIfNotBlank(j, "Twint bénéficiaire", achat.getNumeroTwintBeneficiaire());
    appendIfNotBlank(j, "Adresse wallet crypto", achat.getAdresseWalletCrypto());
    appendIfNotBlank(j, "Hash transaction crypto", achat.getHashTransactionCrypto());
    appendIfNotBlank(j, "Bénéficiaire société", achat.getSocieteBeneficiaire());
    appendIfNotBlank(j, "Bénéficiaire prénom", achat.getPrenomBeneficiaire());
    appendIfNotBlank(j, "Bénéficiaire nom", achat.getNomBeneficiaire());
    appendBooleanAsOuiNon(j, "Preuve de paiement indisponible", achat.getPreuvePaiementIndisponible());
    appendIfNotBlank(j, "Raison absence preuve paiement", achat.getRaisonAbsencePreuvePaiement());
    appendBooleanAsOuiNon(j, "Copie identité transmise à l'auteur", achat.getCopieIdentiteTransmiseAuteur());
    appendBooleanAsOuiNon(j, "Copie identité auteur transmise", achat.getCopieIdentiteAuteurTransmise());
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

  private String buildCommandeFrauduleuseCounterpartyPersonAdditionalInformation(
      Cybercrime cybercrime,
      CommandeFrauduleuse commande
  ) {
    if (cybercrime == null || commande == null) {
      return null;
    }
    StringJoiner j = new StringJoiner(" | ");
    if (commande.getMontant() != null) {
      appendIfNotBlank(j, "Montant", commande.getMontant().toString());
    }
    appendIfNotBlank(j, "Date découverte", commande.getDateDecouverte());
    if (commande.getAssurance() != null) {
      j.add("Assurance: " + (Boolean.TRUE.equals(commande.getAssurance()) ? "oui" : "non"));
    }
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
        && (communication.getUri() == null || communication.getUri().isBlank()));
  }

  private record CyberCounterpartyData(
      String officialName,
      String firstName,
      String email,
      String phone,
      String uri,
      Adresse address,
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
            && isCounterpartyAdresseEmpty(address)
            && isBlank(personAdditionalInformation);
      }
      return isBlank(officialName)
          && isBlank(firstName)
          && isBlank(email)
          && isBlank(phone)
          && isBlank(uri)
          && isCounterpartyAdresseEmpty(address)
          && isBlank(personAdditionalInformation)
          && isBlank(identityAdditionalInformation);
    }

    private boolean isBlank(String value) {
      return value == null || value.isBlank();
    }
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
