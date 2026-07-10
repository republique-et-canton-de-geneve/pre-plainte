package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.BusinessCase;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Relations;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.strip();
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

  private record CyberCounterpartyUriData(String uri, String uriProvider) {
  }
}
