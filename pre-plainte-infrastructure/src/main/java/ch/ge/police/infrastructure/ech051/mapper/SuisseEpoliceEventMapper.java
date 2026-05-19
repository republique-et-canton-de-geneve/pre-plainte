package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
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
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ActionPeriod;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ActionPlace;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ProcessData;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.StringJoiner;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.MODUS_OPERANDI;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_CRIME;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_LIEU;

/**
 * Mapper dédié à la transformation des événements (incidents)
 * du domaine métier vers le format eCH-0051.
 */
@Component
@RequiredArgsConstructor
public class SuisseEpoliceEventMapper {

  private final SuisseEpoliceAddressMapper addressMapper;

  /**
   * Construit les données de traitement (ProcessData).
   */
  public ProcessData buildProcessData(IncidentBase incident, String sourceValue) {
    String sourceTable = determineSourceTable(incident);

    return ProcessData.builder()
        .deliveryDate(LocalDate.now().toString())
        .sourceId(sourceTable)
        .sourceValue(sourceValue)
        .processingStatus(Ech051Constants.PROCESSING_STATUS_GREEN)
        .build();
  }

  private String determineSourceTable(IncidentBase incident) {
    if (incident instanceof Vol) {
      return Ech051Constants.SourceIds.VOL;
    }
    if (incident instanceof DommageMateriel) {
      return Ech051Constants.SourceIds.DOMMAGE_MATERIEL;
    }
    if (incident instanceof Cybercrime cybercrime) {
      return resolveCybercrimeSourceTable(cybercrime);
    }
    if (incident != null) {
      TypeIncident typeIncident = incident.getTypeIncident();
      if (typeIncident == TypeIncident.DOMMAGE) {
        return Ech051Constants.SourceIds.DOMMAGE_MATERIEL;
      }
      if (typeIncident == TypeIncident.VOL) {
        return Ech051Constants.SourceIds.VOL;
      }
      if (typeIncident == TypeIncident.CYBER) {
        return Ech051Constants.SourceIds.CYBERCRIME;
      }
      return extractIncidentTypeValue(typeIncident);
    }
    return Ech051Constants.SourceIds.UNKNOWN;
  }

  private String resolveCybercrimeSourceTable(Cybercrime cybercrime) {
    if (isCyberAchatNonRecu(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_ACHAT_NON_RECU;
    }
    if (isCyberCommandeFrauduleuse(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_COMMANDE_FRAUDULEUSE;
    }
    if (isCyberFausseAnnonce(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_FAUSSE_ANNONCE;
    }
    return Ech051Constants.SourceIds.CYBERCRIME;
  }

  /**
   * Construit l'événement principal à partir de l'incident.
   */
  public Event buildEvent(IncidentBase incident, InformationsPersonnelles infos) {
    if (incident instanceof Cybercrime cybercrime && isCyberTransactionType(cybercrime)) {
      return buildCyberTransactionEvent(cybercrime);
    }

    Adresse primaryActionAddress = incident.getAdresseIncident();
    if (primaryActionAddress == null || !addressMapper.isAddressComplete(primaryActionAddress)) {
      primaryActionAddress = infos != null ? infos.getAdresse() : null;
    }

    Adresse secondaryActionAddress = incident.getAdresseIncidentSecondaire();
    if (secondaryActionAddress != null && !addressMapper.isAddressComplete(secondaryActionAddress)) {
      secondaryActionAddress = null;
    }

    return Event.builder()
      .key(Ech051Constants.EVENT_KEY)
      .descriptionShort(determineEventDescription(incident))
      .complaintDate(LocalDate.now().toString())
      .actionPeriod(ActionPeriod.builder()
        .from(incident.getDateDebutEvent())
        .to(incident.getDateFinEvent())
        .build())
      .actionPlace(buildActionPlace(primaryActionAddress))
      .secondaryActionPlace(buildActionPlace(secondaryActionAddress))
      .bootyAmount(buildBootyAmount(incident))
      .locality(buildLocalityReference(incident))
      .modeOperandi(buildModeOperandiReference(incident))
      .typeOfCrime(buildTypeOfCrimeReference(incident))
      .facts(buildEventFacts(incident))
      .additionalInformation(buildEventAdditionalInformation(incident))
      .build();
  }

  private Event buildCyberTransactionEvent(Cybercrime incident) {
    RipolLocation unknownCountry = RipolLocation.builder()
        .code(Ech051Constants.COUNTRY_UNKNOWN_RIPOL_CODE)
        .label(Ech051Constants.COUNTRY_UNKNOWN_LABEL)
        .sourceTable("EXT_GPNATI")
        .zipCode(null)
        .build();

    ActionPlace cyberPlace = ActionPlace.builder()
        .country(unknownCountry)
        .build();

    return Event.builder()
        .key(Ech051Constants.EVENT_KEY)
        .descriptionShort(determineEventDescription(incident))
        .complaintDate(LocalDate.now().toString())
        .actionPeriod(resolveCyberActionPeriod(incident))
        .actionPlace(cyberPlace)
        .secondaryActionPlace(null)
        .bootyAmount(buildBootyAmount(incident))
        .locality(buildLocalityReference(incident))
        .modeOperandi(buildModeOperandiReference(incident))
        .typeOfCrime(buildTypeOfCrimeReference(incident))
        .facts(buildEventFacts(incident))
        .additionalInformation(buildEventAdditionalInformation(incident))
        .build();
  }

  private ActionPeriod resolveCyberActionPeriod(Cybercrime cybercrime) {
    String from = cybercrime.getDateDebutEvent();
    String to = cybercrime.getDateFinEvent();

    if (cybercrime.getAchatNonRecu() != null || cybercrime.getFausseAnnonce() != null) {
      if (cybercrime.getDatePremierContact() != null && !cybercrime.getDatePremierContact().isBlank()) {
        from = cybercrime.getDatePremierContact();
      }
      if (cybercrime.getDateDernierContact() != null && !cybercrime.getDateDernierContact().isBlank()) {
        to = cybercrime.getDateDernierContact();
      } else if (cybercrime.getDatePremierContact() != null && !cybercrime.getDatePremierContact().isBlank()) {
        to = cybercrime.getDatePremierContact();
      }
    } else if (cybercrime.getCommandeFrauduleuse() != null) {
      CommandeFrauduleuse cf = cybercrime.getCommandeFrauduleuse();
      if (cf.getDateDecouverte() != null && !cf.getDateDecouverte().isBlank()) {
        from = cf.getDateDecouverte();
        to = cf.getDateDecouverte();
      }
    }

    return ActionPeriod.builder()
        .from(from)
        .to(to)
        .build();
  }

  private boolean isCyberTransactionType(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return isCyberCommandeFrauduleuse(cybercrime)
        || isCyberAchatNonRecu(cybercrime)
        || isCyberFausseAnnonce(cybercrime);
  }

  /**
   * Construit le lieu de l'action.
   */
  public ActionPlace buildActionPlace(Adresse adresse) {
    if (adresse == null) {
      return null;
    }
    RipolLocation place = addressMapper.buildAddressPlace(adresse);
    String cityArea = null;
    if (place != null && place.getLabel() != null && !place.getLabel().isBlank()) {
      cityArea = place.getLabel().strip();
    }
    return ActionPlace.builder()
      .street(adresse.adresse())
      .houseNumber(null)
      .place(place)
      .cityArea(cityArea)
      .build();
  }

  /**
   * Extrait le montant du butin (vol) ou des dommages.
   */
  public String buildBootyAmount(IncidentBase incident) {
    if (incident instanceof DommageMateriel dommage && dommage.getMontantEstime() != null) {
      return String.valueOf(dommage.getMontantEstime().intValue());
    }
    return null;
  }

  /**
   * Construit la référence RIPOL pour le type de lieu.
   */
  public RipolReference buildLocalityReference(IncidentBase incident) {
    if (incident.getTypeLieu() != null && incident.getTypeLieu().code() != null) {
      return RipolReferenceBuilder.of(
          incident.getTypeLieu().code(),
          incident.getTypeLieu().label(),
          TYPE_LIEU
      );
    }
    return null;
  }

  /**
   * Construit la référence RIPOL pour le mode opératoire (nature du dommage).
   */
  public RipolReference buildModeOperandiReference(IncidentBase incident) {
    if (incident instanceof Cybercrime cybercrime && isCyberAchatNonRecu(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.MODE_OPERANDI_CYBER_ACHAT_NON_RECU_CODE,
          Ech051Constants.MODE_OPERANDI_CYBER_ACHAT_NON_RECU_LABEL,
          MODUS_OPERANDI
      );
    }
    if (incident instanceof Cybercrime cybercrime && isCyberCommandeFrauduleuse(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.MODE_OPERANDI_CYBER_COMMANDE_FRAUDULEUSE_CODE,
          Ech051Constants.MODE_OPERANDI_CYBER_COMMANDE_FRAUDULEUSE_LABEL,
          MODUS_OPERANDI
      );
    }
    if (incident instanceof Cybercrime cybercrime && isCyberFausseAnnonce(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.MODE_OPERANDI_CYBER_FAUSSE_ANNONCE_CODE,
          Ech051Constants.MODE_OPERANDI_CYBER_FAUSSE_ANNONCE_LABEL,
          MODUS_OPERANDI
      );
    }

    if (!(incident instanceof DommageMateriel dommage) || dommage.getNaturesDommage() == null || dommage.getNaturesDommage().isEmpty()) {
      return null;
    }

    NatureDommage nature = dommage.getNaturesDommage().getFirst();
    if (nature == NatureDommage.DEGRADATIONS) {
      return RipolReferenceBuilder.of(
          Ech051Constants.MODE_OPERANDI_DEGRADATIONS_CODE,
          Ech051Constants.MODE_OPERANDI_DEGRADATIONS_LABEL,
          MODUS_OPERANDI
      );
    }
    if (nature == NatureDommage.TAGS_GRAFFITI) {
      return RipolReferenceBuilder.of(
          Ech051Constants.MODE_OPERANDI_TAGS_CODE,
          Ech051Constants.MODE_OPERANDI_TAGS_LABEL,
          MODUS_OPERANDI
      );
    }
    return null;
  }

  /**
   * Construit la référence RIPOL pour le type de crime.
   */
  public RipolReference buildTypeOfCrimeReference(IncidentBase incident) {
    if (incident instanceof Vol) {
      return RipolReferenceBuilder.of(
          Ech051Constants.TYPE_OF_CRIME_VOL_CODE,
          Ech051Constants.TYPE_OF_CRIME_VOL_LABEL,
          TYPE_CRIME
      );
    }
    if (incident instanceof DommageMateriel) {
      return RipolReferenceBuilder.of(
          Ech051Constants.TYPE_OF_CRIME_DOMMAGE_CODE,
          Ech051Constants.TYPE_OF_CRIME_DOMMAGE_LABEL,
          TYPE_CRIME
      );
    }
    if (incident instanceof Cybercrime cybercrime && isCyberAchatNonRecu(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.TYPE_OF_CRIME_CYBER_ACHAT_NON_RECU_CODE,
          Ech051Constants.TYPE_OF_CRIME_CYBER_ACHAT_NON_RECU_LABEL,
          TYPE_CRIME
      );
    }
    if (incident instanceof Cybercrime cybercrime && isCyberCommandeFrauduleuse(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_CODE,
          Ech051Constants.TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_LABEL,
          TYPE_CRIME
      );
    }
    if (incident instanceof Cybercrime cybercrime && isCyberFausseAnnonce(cybercrime)) {
      return RipolReferenceBuilder.of(
          Ech051Constants.TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_CODE,
          Ech051Constants.TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_LABEL,
          TYPE_CRIME
      );
    }
    return null;
  }

  private String determineEventDescription(IncidentBase incident) {
    if (incident instanceof Cybercrime cybercrime && isCyberAchatNonRecu(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_ACHAT_NON_RECU;
    }
    if (incident instanceof Cybercrime cybercrime && isCyberCommandeFrauduleuse(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_COMMANDE_FRAUDULEUSE;
    }
    if (incident instanceof Cybercrime cybercrime && isCyberFausseAnnonce(cybercrime)) {
      return Ech051Constants.SourceIds.CYBERCRIME_FAUSSE_ANNONCE;
    }
    if (incident instanceof DommageMateriel) {
      return Ech051Constants.SourceIds.DOMMAGE_MATERIEL;
    }
    return extractIncidentTypeValue(incident.getTypeIncident());
  }

  private String buildEventFacts(IncidentBase incident) {
    if (incident instanceof Cybercrime cybercrime) {
      String description = cybercrime.getDescriptionCybercrime();
      if (description != null && !description.isBlank()) {
        return description;
      }
    }
    return null;
  }

  /**
   * Construit les informations additionnelles de l'événement.
   */
  public String buildEventAdditionalInformation(IncidentBase incident) {
    if (incident == null) {
      return null;
    }

    if (incident instanceof DommageMateriel dommage) {
      String description = dommage.getDescription();
      if (description != null && !description.isBlank()) {
        return description;
      }
    }

    if (incident instanceof Cybercrime cybercrime) {
      if (!isCyberAchatNonRecu(cybercrime)) {
        return null;
      }
      return buildAchatNonRecuAdditionalInformation(cybercrime.getAchatNonRecu());
    }

    return null;
  }

  private String buildAchatNonRecuAdditionalInformation(AchatNonRecu achat) {
    if (achat == null) {
      return null;
    }

    StringJoiner details = new StringJoiner(" | ");
    addIfNotBlank(details, "Montant du delit", achat.getMontantDelitAchatLigne());
    addIfNotBlank(details, "Article non livre", achat.getArticleNonLivreDescription());
    addIfNotBlank(details, "Email vendeur inconnu", formatBoolean(achat.getEmailVendeurInconnu()));
    addIfNotBlank(details, "Achat via place de marche", formatBoolean(achat.getAchatViaPlaceMarche()));
    addIfNotBlank(details, "ID plateforme", achat.getPlateformeId());
    addIfNotBlank(details, "Nom entreprise vendeur", achat.getNomEntrepriseVendeur());
    addIfNotBlank(details, "Site web entreprise vendeur", achat.getSiteWebEntrepriseVendeur());
    addIfNotBlank(details, "Annonce indisponible", formatBoolean(achat.getAnnonceDocumentIndisponible()));
    addIfNotBlank(details, "Raison absence annonce", achat.getRaisonAbsenceAnnonce());
    addIfNotBlank(details, "IBAN beneficiaire", achat.getIbanBeneficiaire());
    addIfNotBlank(details, "Compte PayPal beneficiaire", achat.getComptePaypalBeneficiaire());
    addIfNotBlank(details, "Numero Twint beneficiaire", achat.getNumeroTwintBeneficiaire());
    addIfNotBlank(details, "Adresse wallet crypto", achat.getAdresseWalletCrypto());
    addIfNotBlank(details, "Hash transaction crypto", achat.getHashTransactionCrypto());
    addIfNotBlank(details, "Preuve paiement indisponible", formatBoolean(achat.getPreuvePaiementIndisponible()));
    addIfNotBlank(details, "Raison absence preuve paiement", achat.getRaisonAbsencePreuvePaiement());
    addIfNotBlank(details, "Copie identite transmise a l'auteur", formatBoolean(achat.getCopieIdentiteTransmiseAuteur()));
    addIfNotBlank(details, "Copie identite auteur transmise", formatBoolean(achat.getCopieIdentiteAuteurTransmise()));

    String value = details.toString();
    return value.isBlank() ? null : value;
  }

  private void addIfNotBlank(StringJoiner details, String label, String value) {
    if (value != null && !value.isBlank()) {
      details.add(label + ": " + value);
    }
  }

  private String formatBoolean(Boolean value) {
    if (value == null) {
      return null;
    }
    return value ? "oui" : "non";
  }

  /**
   * Extrait la valeur du type d'incident.
   */
  public String extractIncidentTypeValue(TypeIncident typeIncident) {
    return typeIncident != null ? typeIncident.jsonValue() : null;
  }

  private boolean isCyberCommandeFrauduleuse(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return cybercrime.getTypeCybercrime() == TypeCybercrime.COMMANDE_FRAUDULEUSE;
  }

  private boolean isCyberFausseAnnonce(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return cybercrime.getTypeCybercrime() == TypeCybercrime.FAUSSE_ANNONCE;
  }

  private boolean isCyberAchatNonRecu(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return false;
    }
    return cybercrime.getTypeCybercrime() == TypeCybercrime.ACHAT_NON_RECU;
  }
}
