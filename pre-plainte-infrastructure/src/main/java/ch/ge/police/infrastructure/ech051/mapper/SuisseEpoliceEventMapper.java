package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.MyAbiAdditionalInformationFormatter;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ActionPeriod;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ActionPlace;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Event;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ProcessData;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
   * Construit la liste des événements (2 pour achat non reçu, 1 sinon).
   */
  public List<Event> buildEvents(IncidentBase incident, InformationsPersonnelles infos) {
    if (incident == null) {
      return List.of();
    }
    if (incident instanceof Cybercrime cybercrime && isCyberAchatNonRecu(cybercrime)) {
      return buildCyberAchatNonRecuEvents(cybercrime, infos);
    }
    if (incident instanceof Cybercrime cybercrime && isCyberCommandeFrauduleuse(cybercrime)) {
      return buildCyberCommandeFrauduleuseEvents(cybercrime, infos);
    }
    return List.of(buildEvent(incident, infos));
  }

  /**
   * Construit l'événement principal à partir de l'incident.
   */
  public Event buildEvent(IncidentBase incident, InformationsPersonnelles infos) {
    if (incident instanceof Cybercrime cybercrime && isCyberAchatNonRecu(cybercrime)) {
      return buildCyberAchatNonRecuEvents(cybercrime, infos).getFirst();
    }
    if (incident instanceof Cybercrime cybercrime && isCyberCommandeFrauduleuse(cybercrime)) {
      return buildCyberCommandeFrauduleuseEvents(cybercrime, infos).getFirst();
    }
    if (incident instanceof Cybercrime cybercrime && isCyberTransactionType(cybercrime)) {
      return buildCyberTransactionEvent(cybercrime, infos);
    }

    ActionPlace primaryActionPlace;
    if (isPublicPlaceLocation(incident)) {
      primaryActionPlace = buildActionPlaceForPublicLocation(incident);
    } else {
      Adresse primaryActionAddress = incident.getAdresseIncident();
      if (primaryActionAddress == null || !addressMapper.isAddressComplete(primaryActionAddress)) {
        primaryActionAddress = infos != null ? infos.getAdresse() : null;
      }
      primaryActionPlace = buildActionPlace(primaryActionAddress);
    }

    Adresse secondaryActionAddress = incident.getAdresseIncidentSecondaire();
    if (secondaryActionAddress != null && !addressMapper.isAddressComplete(secondaryActionAddress)) {
      secondaryActionAddress = null;
    }

    return Event.builder()
      .key(Ech051Constants.EVENT_KEY)
      .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
      .complaintDate(LocalDate.now().toString())
      .actionPeriod(ActionPeriod.builder()
        .from(incident.getDateDebutEvent())
        .to(incident.getDateFinEvent())
        .build())
      .actionPlace(primaryActionPlace)
      .secondaryActionPlace(buildActionPlace(secondaryActionAddress))
      .bootyAmount(buildBootyAmount(incident))
      .locality(isPublicPlaceLocation(incident) ? null : buildLocalityReference(incident))
      .additionalInformation(buildEventAdditionalInformation(incident))
      .build();
  }

  private Event buildCyberTransactionEvent(Cybercrime incident, InformationsPersonnelles infos) {
    return Event.builder()
        .key(Ech051Constants.EVENT_KEY)
        .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
        .complaintDate(LocalDate.now().toString())
        .actionPeriod(resolveCyberActionPeriod(incident))
        .actionPlace(resolveCyberActionPlace(infos))
        .secondaryActionPlace(null)
        .bootyAmount(buildBootyAmount(incident))
        .locality(buildLocalityReference(incident))
        .facts(resolveCyberFacts(incident))
        .additionalInformation(buildEventAdditionalInformation(incident))
        .build();
  }

  private List<Event> buildCyberAchatNonRecuEvents(Cybercrime incident, InformationsPersonnelles infos) {
    ActionPeriod actionPeriod = resolveCyberActionPeriod(incident);
    String complaintDate = LocalDate.now().toString();
    String facts = resolveCyberFacts(incident);

    Event deliveryEvent = Event.builder()
        .key(Ech051Constants.EVENT_KEY)
        .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
        .complaintDate(complaintDate)
        .actionPeriod(actionPeriod)
        .actionPlace(resolveCyberActionPlace(infos))
        .facts(facts)
        .additionalInformation(null)
        .build();

    Event paymentEvent = Event.builder()
        .key(Ech051Constants.EVENT_KEY_PAYMENT)
        .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
        .complaintDate(complaintDate)
        .actionPeriod(actionPeriod)
        .actionPlace(buildUnknownCountryActionPlace())
        .facts(facts)
        .additionalInformation(null)
        .build();

    return List.of(deliveryEvent, paymentEvent);
  }

  private List<Event> buildCyberCommandeFrauduleuseEvents(Cybercrime incident, InformationsPersonnelles infos) {
    ActionPeriod actionPeriod = resolveCyberActionPeriod(incident);
    String complaintDate = LocalDate.now().toString();
    String facts = resolveCyberFacts(incident);

    Event deliveryEvent = Event.builder()
        .key(Ech051Constants.EVENT_KEY)
        .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
        .complaintDate(complaintDate)
        .actionPeriod(actionPeriod)
        .actionPlace(resolveCyberActionPlace(infos))
        .facts(facts)
        .additionalInformation(null)
        .build();

    Event secondEvent = Event.builder()
        .key(Ech051Constants.EVENT_KEY_PAYMENT)
        .descriptionShort(Ech051Constants.PRE_PLAINTE_EN_LIGNE)
        .complaintDate(complaintDate)
        .actionPeriod(actionPeriod)
        .actionPlace(buildUnknownCountryActionPlace())
        .facts(facts)
        .additionalInformation(null)
        .build();

    return List.of(deliveryEvent, secondEvent);
  }

  private ActionPlace buildUnknownCountryActionPlace() {
    RipolLocation unknownCountry = RipolLocation.builder()
        .code(Ech051Constants.COUNTRY_UNKNOWN_RIPOL_CODE)
        .label(Ech051Constants.COUNTRY_UNKNOWN_LABEL)
        .sourceTable("EXT_GPNATI")
        .zipCode(null)
        .build();
    return ActionPlace.builder()
        .country(unknownCountry)
        .build();
  }

  private String resolveCyberFacts(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return null;
    }
    String description = cybercrime.getDescriptionCybercrime();
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.strip();
  }

  private ActionPlace resolveCyberActionPlace(InformationsPersonnelles infos) {
    Adresse victimAddress = resolveVictimAddress(infos);
    if (addressMapper.isAddressComplete(victimAddress)) {
      return buildActionPlace(victimAddress);
    }

    RipolLocation unknownCountry = RipolLocation.builder()
        .code(Ech051Constants.COUNTRY_UNKNOWN_RIPOL_CODE)
        .label(Ech051Constants.COUNTRY_UNKNOWN_LABEL)
        .sourceTable("EXT_GPNATI")
        .zipCode(null)
        .build();

    return ActionPlace.builder()
        .country(unknownCountry)
        .build();
  }

  private Adresse resolveVictimAddress(InformationsPersonnelles infos) {
    if (infos == null) {
      return null;
    }
    if (infos.hasOrganisation() && infos.getOrganisation() != null) {
      return infos.getOrganisation().getAdresse();
    }
    if (infos.hasTiers() && infos.getTiers() != null) {
      return infos.getTiers().getAdresse();
    }
    return infos.getAdresse();
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
   * Lieu public (plage, parc, etc.) : rue = nom du lieu, place = commune, sans numéro ni locality séparée.
   */
  public ActionPlace buildActionPlaceForPublicLocation(IncidentBase incident) {
    if (incident.getTypeLieu() == null) {
      return null;
    }
    String commune = resolvePublicPlaceCommune(incident);
    RipolLocation place = null;
    if (commune != null && !commune.isBlank()) {
      place = RipolLocation.builder()
          .label(commune.strip())
          .build();
    }
    return ActionPlace.builder()
        .street(capitalizeFirst(incident.getTypeLieu().label()))
        .houseNumber(null)
        .place(place)
        .cityArea(null)
        .build();
  }

  private boolean isPublicPlaceLocation(IncidentBase incident) {
    return incident.getTypeLieu() != null
        && Boolean.FALSE.equals(incident.getAdresseConnue())
        && !Boolean.TRUE.equals(incident.getIsTrajet());
  }

  private String resolvePublicPlaceCommune(IncidentBase incident) {
    if (incident.getLieuOrigine() != null && !incident.getLieuOrigine().isBlank()) {
      return incident.getLieuOrigine();
    }
    Adresse adresseIncident = incident.getAdresseIncident();
    if (adresseIncident != null && adresseIncident.localite() != null && !adresseIncident.localite().isBlank()) {
      return adresseIncident.localite();
    }
    return null;
  }

  private static String capitalizeFirst(String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    String stripped = value.strip();
    if (stripped.length() == 1) {
      return stripped.toUpperCase(Locale.ROOT);
    }
    return stripped.substring(0, 1).toUpperCase(Locale.ROOT) + stripped.substring(1);
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

  /**
   * Construit les informations additionnelles de l'événement.
   */
  public String buildEventAdditionalInformation(IncidentBase incident) {
    if (incident instanceof Cybercrime cybercrime) {
      if (isCyberTransactionType(cybercrime)) {
        return buildCyberTransactionEventAdditionalInformation(cybercrime);
      }
      return buildCyberAutreAdditionalInformation(cybercrime);
    }
    return null;
  }

  private String buildCyberTransactionEventAdditionalInformation(Cybercrime cybercrime) {
    if (isCyberAchatNonRecu(cybercrime) || isCyberCommandeFrauduleuse(cybercrime)) {
      return null;
    }

    List<String> details = MyAbiAdditionalInformationFormatter.builder();
    if (isCyberFausseAnnonce(cybercrime)) {
      appendFausseAnnonceDetails(details, cybercrime.getFausseAnnonce());
    }
    return MyAbiAdditionalInformationFormatter.format(details);
  }

  private String buildCyberAutreAdditionalInformation(Cybercrime cybercrime) {
    if (cybercrime == null) {
      return null;
    }

    List<String> details = MyAbiAdditionalInformationFormatter.builder();
    MyAbiAdditionalInformationFormatter.addLabeled(
        details, "Description du cybercrime", cybercrime.getDescriptionCybercrime());
    return MyAbiAdditionalInformationFormatter.format(details);
  }

  private void appendFausseAnnonceDetails(List<String> details, FausseAnnonce annonce) {
    if (annonce == null) {
      return;
    }
    MyAbiAdditionalInformationFormatter.addLabeled(details, "URL complète", annonce.getUrlComplete());
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Titre de l'annonce", annonce.getTitreAnnonce());
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Nom du bailleur", annonce.getNomBailleur());
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Email bailleur inconnu", formatBoolean(annonce.getEmailBailleurInconnu()));
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Email bailleur", annonce.getEmailBailleur());
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Téléphone bailleur inconnu", formatBoolean(annonce.getTelephoneBailleurInconnu()));
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Téléphone bailleur", annonce.getTelephoneBailleur());
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Adresse du bien immobilier", annonce.getAdresseBienImmobilier());
    if (annonce.getMontantDemande() != null) {
      MyAbiAdditionalInformationFormatter.addLabeled(details, "Montant demandé", annonce.getMontantDemande().toString());
    }
    MyAbiAdditionalInformationFormatter.addLabeled(details, "Mode de paiement demandé", annonce.getModePaiementDemande());
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
