package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.InfosPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Communication;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.LegalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NaturalIdentity;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Person;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonType;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.*;

/**
 * Mapper dédié à la transformation des informations personnelles
 * du domaine métier vers le format eCH-0051.
 */
@Component
@RequiredArgsConstructor
public class SuisseEpolicePersonMapper {

  private final SuisseEpoliceAddressMapper addressMapper;

  /**
   * Construit la liste complète des personnes selon le type de déclaration.
   *
   * MOI_MEME : 1 personne (déclarant = lésé, key=4) ou 2 si véhicule (+assurance key=3)
   * TIERS : 3 personnes (tiers = lésé key=4, déclarant = représentant key=7, assurance key=2)
   * ENTREPRISE : 4 personnes (organisation key=4, déclarant key=6, informant key=8, assurance key=3)
   */
  public List<Person> buildPersons(InformationsPersonnelles infos, boolean hasVehicles, String insurerName) {
    List<Person> persons = new ArrayList<>();

    if (infos == null) {
      return persons;
    }

    if (infos.hasOrganisation() && infos.getOrganisation() != null) {
      buildEntreprisePersons(infos, persons, insurerName);
    } else if (infos.hasTiers() && infos.getTiers() != null) {
      buildTiersPersons(infos, persons, insurerName);
    } else {
      buildIndividualPerson(infos, persons, hasVehicles, insurerName);
    }

    return persons;
  }

  /**
   * Construit les personnes pour une déclaration individuelle (MOI_MEME).
   * Si hasVehicles=true : utilise les keys véhicule et ajoute personne assurance.
   */
  private void buildIndividualPerson(InformationsPersonnelles infos, List<Person> persons, boolean hasVehicles,
                                     String insurerName) {
    if (hasVehicles) {
      persons.add(buildNaturalPerson(
          Ech051Constants.PERSON_KEY_VEHICLE,
          Ech051Constants.IDENTITY_KEY_VEHICLE,
          infos,
          null
      ));
      persons.add(buildInsurancePerson(Ech051Constants.INSURER_KEY_VEHICLE, insurerName));
    } else {
      persons.add(buildNaturalPerson(
          Ech051Constants.PERSON_KEY_TIERS,
          Ech051Constants.IDENTITY_KEY_TIERS,
          infos,
          null
      ));
    }
  }

  /**
   * Construit les personnes pour une déclaration tiers (TIERS).
   * Ordre selon declarer-pour-une-personne.xml :
   * 1. Tiers (natural, lésé) - key=4
   * 2. Déclarant (natural, représentant) - key=7
   * 3. Assurance "aucune" (legal) - key=2
   */
  private void buildTiersPersons(InformationsPersonnelles infos, List<Person> persons, String insurerName) {
    persons.add(buildNaturalPerson(
        Ech051Constants.PERSON_KEY_TIERS,
        Ech051Constants.IDENTITY_KEY_TIERS,
        infos.getTiers(),
        null
    ));
    persons.add(buildNaturalPerson(
        Ech051Constants.PERSON_KEY_DECLARANT,
        Ech051Constants.IDENTITY_KEY_DECLARANT,
        infos,
        null
    ));
    persons.add(buildInsurancePerson(Ech051Constants.PERSON_KEY_ASSURANCE_TIERS, insurerName));
  }

  /**
   * Construit les personnes pour une déclaration entreprise (ENTREPRISE).
   * 1. Organisation (legal) - key=4
   * 2. Déclarant (natural) - key=6
   * 3. Informant simplifié (natural) - key=8
   * 4. Assurance "aucune" (legal) - key=3
   */
  private void buildEntreprisePersons(InformationsPersonnelles infos, List<Person> persons, String insurerName) {
    Organisation org = infos.getOrganisation();

    persons.add(buildOrganisationPerson(Ech051Constants.PERSON_KEY_ORGANISATION, org));
    persons.add(buildNaturalPerson(
        Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE,
        Ech051Constants.IDENTITY_KEY_DECLARANT_ENTREPRISE,
        infos,
        null
    ));
    persons.add(buildSimplifiedInformant(
        Ech051Constants.PERSON_KEY_INFORMANT,
        Ech051Constants.IDENTITY_KEY_INFORMANT,
        infos
    ));
    persons.add(buildInsurancePerson(Ech051Constants.PERSON_KEY_ASSURANCE_ENTREPRISE, insurerName));
  }

  /**
   * Construit une personne morale pour l'organisation.
   */
  private Person buildOrganisationPerson(String key, Organisation org) {
    Communication comm = null;
    if (org.getEmail() != null || org.getTelephone() != null) {
      comm = Communication.builder()
          .email(org.getEmail())
          .phone(org.getTelephone())
          .build();
    }

    return Person.builder()
        .key(key)
        .type(PersonType.LEGAL)
        .legalIdentity(LegalIdentity.builder()
            .currentName(org.getNom())
            .build())
        .address(addressMapper.fromAdresse(org.getAdresse()))
        .communication(comm)
        .build();
  }

  /**
   * Construit un informant simplifié (juste nom, prénom, téléphone, email).
   */
  private Person buildSimplifiedInformant(String personKey, String identityKey, InformationsPersonnelles infos) {
    return Person.builder()
        .key(personKey)
        .type(PersonType.NATURAL)
        .naturalIdentity(NaturalIdentity.builder()
            .key(identityKey)
            .officialName(infos.getNom())
            .firstName(infos.getPrenom())
            .build())
        .communication(buildCommunication(infos))
        .build();
  }

  public Person buildInsurancePerson(String key, String insurerName) {
    String currentName = insurerName != null && !insurerName.isBlank() ? insurerName.trim() : "aucune";
    return Person.builder()
        .key(key)
        .type(PersonType.LEGAL)
        .legalIdentity(LegalIdentity.builder()
            .currentName(currentName)
            .build())
        .build();
  }

  /**
   * Construit une personne physique (natural person).
   */
  public Person buildNaturalPerson(String personKey, String identityKey, InfosPersonne infos, String additionalInfo) {
    return Person.builder()
        .key(personKey)
        .type(PersonType.NATURAL)
        .naturalIdentity(NaturalIdentity.builder()
            .key(identityKey)
            .officialName(infos.getNom())
            .originalName(infos.getNomNaissance())
            .firstName(infos.getPrenom())
            .sex(buildSexReference(infos))
            .birthDate(infos.getDateNaissance())
            .languageCode(resolveLanguage(infos))
            .placeOfOrigin(buildPlaceOfOrigin(infos))
            .nationality(buildNationalityReference(infos))
            .profession(null)
            .build())
        .address(addressMapper.fromAdresse(infos.getAdresse()))
        .communication(buildCommunication(infos))
        .additionalInformation(additionalInfo)
        .build();
  }

  /**
   * Construit le lieu d'origine (pour les personnes suisses).
   */
  public RipolLocation buildPlaceOfOrigin(InfosPersonne infos) {
    if (infos.getLieuOrigineCode() == null) {
      return null;
    }
    return RipolLocation.builder()
        .code(infos.getLieuOrigineCode())
        .label(infos.getLieuOrigineLabel())
        .sourceTable(LIEU_ORIGINE)
        .zipCode(infos.getLieuOrigineCode())
        .build();
  }

  /**
   * Construit la référence RIPOL pour le sexe.
   */
  public RipolReference buildSexReference(InfosPersonne infos) {
    if (infos.getGenreCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.ofIso(infos.getGenreCode(), infos.getGenreLabel(), SEXE);
  }

  /**
   * Construit la référence RIPOL pour la nationalité.
   */
  public RipolReference buildNationalityReference(InfosPersonne infos) {
    if (infos.getNationaliteCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(infos.getNationaliteCode(), infos.getNationaliteLabel(), NATIONALITE);
  }

  /**
   * Construit les informations de communication (téléphone, email).
   */
  public Communication buildCommunication(InfosPersonne infos) {
    String email = infos.getEmail();
    String telephone = infos.getTelephone();

    if (email == null && telephone == null) {
      return null;
    }

    return Communication.builder()
        .email(email)
        .mobile(telephone)
        .build();
  }

  /**
   * Résout la langue de correspondance.
   */
  public String resolveLanguage(InfosPersonne infos) {
    if (infos instanceof InformationsPersonnelles pers && pers.getLangueCorrespondance() != null) {
      return pers.getLangueCorrespondance();
    }
    return Ech051Constants.DEFAULT_LANGUAGE;
  }
}
