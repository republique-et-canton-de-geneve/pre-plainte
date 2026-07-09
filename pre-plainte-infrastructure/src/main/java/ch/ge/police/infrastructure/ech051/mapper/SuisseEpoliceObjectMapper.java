package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.InfosPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Identification;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.OfficialDocument;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NumberPlate;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.MyAbiAdditionalInformationFormatter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.*;

/**
 * Mapper dédié à la transformation des objets volés et véhicules
 * du domaine métier vers le format eCH-0051.
 */
@Component
public class SuisseEpoliceObjectMapper {

  private static final String CATEGORIE_DOCUMENTS = "documents";
  private static final String CATEGORIE_PLAQUE = "plaque";
  private static final int OBJECT_KEY_INDEX_OFFSET = 50;
  private static final int VEHICLE_KEY_INDEX_OFFSET = 60;

  /**
   * Construit la liste des objets (non-véhicules) à partir de l'incident.
   */
  public List<ObjectItem> buildObjectsFromIncident(IncidentBase incident) {
    if (incident instanceof Vol vol) {
      return buildObjectsFromVol(vol);
    }

    if (incident instanceof DommageMateriel dommage) {
      return buildObjectsFromDommage(dommage);
    }

    return List.of();
  }

  /**
   * Objets cybercrime : pièce d'identité du lésé (key=6), montant commande frauduleuse (key=9)
   * ou article non livré achat non reçu (key=11).
   */
  public List<ObjectItem> buildCyberObjects(Cybercrime cybercrime, InformationsPersonnelles infos) {
    if (cybercrime == null) {
      return List.of();
    }
    List<ObjectItem> objects = new ArrayList<>();

    ObjectItem identityObject = buildVictimIdentityObject(resolveVictimInfos(infos));
    if (identityObject != null) {
      objects.add(identityObject);
    }

    AchatNonRecu achat = cybercrime.getAchatNonRecu();
    if (achat != null
        && (!isBlank(achat.getMontantDelitAchatLigne()) || !isBlank(achat.getArticleNonLivreDescription()))) {
      objects.add(buildUndeliveredItemObject(achat));
    }

    CommandeFrauduleuse commande = cybercrime.getCommandeFrauduleuse();
    if (commande != null && commande.getMontant() != null) {
      objects.add(buildFraudulentOrderAmountObject(commande));
    }

    return objects;
  }

  private static InfosPersonne resolveVictimInfos(InformationsPersonnelles infos) {
    if (infos == null) {
      return null;
    }
    if (infos.hasTiers() && infos.getTiers() != null) {
      return infos.getTiers();
    }
    return infos;
  }

  private ObjectItem buildVictimIdentityObject(InfosPersonne victimInfos) {
    if (victimInfos == null || victimInfos.getTypeDocumentIdentite() == null) {
      return null;
    }
    if (victimInfos.getTypeDocumentIdentite() == TypeDocumentIdentite.DOCUMENTS_VOLES_PERDUS) {
      return null;
    }
    String documentNumber = trimToNull(victimInfos.getNumeroDocumentIdentite());
    if (documentNumber == null) {
      return null;
    }
    String identificationType = CyberVictimIdentityRipolMapper.mapIdentificationType(
        victimInfos.getTypeDocumentIdentite()
    );
    if (identificationType == null) {
      return null;
    }

    RipolReference permitCategory = CyberVictimIdentityRipolMapper.mapPermitCategory(victimInfos.getTitreSejour());
    OfficialDocument officialDocument = permitCategory != null
        ? OfficialDocument.builder().permitCategory(permitCategory).build()
        : null;

    return ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY)
        .typeOfObject(RipolReferenceBuilder.of(
            Ech051Constants.OBJECT_TYPE_CYBER_IDENTITY_CODE,
            Ech051Constants.OBJECT_TYPE_CYBER_IDENTITY_LABEL,
            TYPE_OBJET
        ))
        .officialDocument(officialDocument)
        .identification(Identification.builder()
            .type(identificationType)
            .number(documentNumber)
            .build())
        .build();
  }

  private ObjectItem buildUndeliveredItemObject(AchatNonRecu achat) {
    return ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_UNDELIVERED_ITEM)
        .description(trimToNull(achat.getArticleNonLivreDescription()))
        .realValue(normalizeAmountForSep(achat.getMontantDelitAchatLigne()))
        .purchaseDate(trimToNull(achat.getDateOperation()))
        .build();
  }

  private ObjectItem buildFraudulentOrderAmountObject(CommandeFrauduleuse commande) {
    return ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_CYBER_FRAUDULENT_ORDER_AMOUNT)
        .realValue(normalizeAmountFromDouble(commande.getMontant()))
        .purchaseDate(trimToNull(commande.getDateDecouverte()))
        .build();
  }

  private static String normalizeAmountFromDouble(Double amount) {
    if (amount == null) {
      return null;
    }
    BigDecimal value = BigDecimal.valueOf(amount);
    if (value.scale() <= 0 || value.stripTrailingZeros().scale() <= 0) {
      return value.toBigInteger().toString();
    }
    return value.stripTrailingZeros().toPlainString();
  }

  private static String normalizeAmountForSep(String amount) {
    if (amount == null || amount.isBlank()) {
      return null;
    }
    String normalized = amount.strip().replace("'", "").replace("\u00a0", "").replace(" ", "");
    if (normalized.contains(",")) {
      normalized = normalized.replace(",", ".");
    }
    try {
      BigDecimal value = new BigDecimal(normalized);
      if (value.scale() <= 0 || value.stripTrailingZeros().scale() <= 0) {
        return value.toBigInteger().toString();
      }
      return value.stripTrailingZeros().toPlainString();
    } catch (NumberFormatException ignored) {
      return amount.strip();
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.strip();
  }

  private List<ObjectItem> buildObjectsFromVol(Vol vol) {
    if (vol.getObjetsVoles() == null) {
      return List.of();
    }

    List<ObjetIncident> nonVehicles = vol.getObjetsVoles().stream()
        .filter(this::isObjectEligibleForMyAbi)
        .toList();
    int n = nonVehicles.size();
    return IntStream.range(0, n)
        .mapToObj(i -> buildObjectItem(nonVehicles.get(i), objectXmlKey(i, n)))
        .toList();
  }

  private ObjectItem buildObjectItem(ObjetIncident objet, String objectKey) {
    return ObjectItem.builder()
        .key(objectKey)
        .typeOfObject(buildObjectTypeReference(objet))
        .fabricant(buildBrandReference(objet))
        .fabricantAutre(objet.getFabricantAutre())
        .modele(buildModelReference(objet))
        .modeleAutre(objet.getModeleAutre())
        .couleur(buildColourReference(objet))
        .couleurSecondaire(buildColourSecondaireReference(objet))
        .realValue(objet.getRealValue())
        .purchaseDate(objet.getPurchaseDate())
        .numeroSerie(numeroSerieForMyAbi(objet))
        .gravure(gravureForMyAbi(objet))
        .identification(buildIdentification(objet))
        .additionalInformation(buildObjectAdditionalInfo(objet))
        .build();
  }

  private List<ObjectItem> buildObjectsFromDommage(DommageMateriel dommage) {
    if (dommage.getObjetDegrades() == null || dommage.getObjetDegrades().isEmpty()) {
      return List.of();
    }
    List<ObjetIncident> eligible = dommage.getObjetDegrades().stream()
        .filter(obj -> !obj.isVehicleType())
        .filter(this::isObjectEligibleForMyAbi)
        .toList();
    int n = eligible.size();
    return IntStream.range(0, n)
        .mapToObj(i -> buildObjectItem(eligible.get(i), objectXmlKey(i, n)))
        .toList();
  }

  /**
   * Construit la liste des véhicules à partir de l'incident.
   */
  public List<VehicleItem> buildVehiclesFromIncident(IncidentBase incident) {
    if (incident instanceof Vol vol) {
      if (vol.getObjetsVoles() == null) {
        return List.of();
      }
      List<ObjetIncident> vehicles = vol.getObjetsVoles().stream()
          .filter(ObjetIncident::isVehicleType)
          .filter(this::hasVehicleIdentificationForMyAbi)
          .toList();
      int n = vehicles.size();
      return IntStream.range(0, n)
          .mapToObj(i -> buildVehicleItem(vehicles.get(i), vehicleXmlKey(i, n)))
          .toList();
    }

    if (incident instanceof DommageMateriel dommage) {
      if (dommage.getObjetDegrades() == null) {
        return List.of();
      }
      List<ObjetIncident> vehicles = dommage.getObjetDegrades().stream()
          .filter(ObjetIncident::isVehicleType)
          .filter(this::hasVehicleIdentificationForMyAbi)
          .toList();
      int n = vehicles.size();
      return IntStream.range(0, n)
          .mapToObj(i -> buildVehicleItem(vehicles.get(i), vehicleXmlKey(i, n)))
          .toList();
    }

    return List.of();
  }

  private VehicleItem buildVehicleItem(ObjetIncident objet, String vehicleKey) {
    return VehicleItem.builder()
        .key(vehicleKey)
        .typeOfVehicle(buildVehicleTypeReference(objet))
        .vin(objet.getVin())
        .frameNumber(objet.getNumeroCadre())
        .mark(buildBrandReference(objet))
        .markOther(objet.getFabricantAutre())
        .modelType(buildModelReference(objet))
        .modelOther(objet.getModeleAutre())
        .colour(buildVehicleColourReference(objet))
        .colourSecondary(buildVehicleColourSecondaireReference(objet))
        .velofinderId(objet.getVelofinderId())
        .purchaseDate(objet.getPurchaseDate())
        .vignetteNumber(blankToNull(objet.getNumeroVignette()))
        .masterNumber(blankToNull(objet.getNumeroMaster()))
        .insuranceNumber(blankToNull(objet.getNumeroAssurance()))
        .additionalInformation(buildVehicleAdditionalInfo(objet))
        .numberPlate(buildNumberPlate(objet))
        .build();
  }

  /**
   * Construit la plaque d'immatriculation pour un véhicule.
   */
  private NumberPlate buildNumberPlate(ObjetIncident objet) {
    if (!hasPlaqueNumeroForMyAbi(objet)) {
      return null;
    }
    return NumberPlate.builder()
        .number(objet.getPlaqueNumero())
        .country(buildPlaquePays(objet))
        .canton(buildPlaqueCanton(objet))
        .build();
  }

  /**
   * Construit la référence RIPOL pour le pays de la plaque.
   */
  private RipolLocation buildPlaquePays(ObjetIncident objet) {
    if (objet.getPlaquePaysCode() == null) {
      return null;
    }
    return RipolLocation.builder()
        .code(objet.getPlaquePaysCode())
        .label(objet.getPlaquePaysLabel())
        .sourceTable(Ech051Constants.RipolSourceTables.PLAQUE_PAYS)
        .zipCode(null)
        .build();
  }

  /**
   * Construit la référence RIPOL pour le canton de la plaque.
   */
  private RipolLocation buildPlaqueCanton(ObjetIncident objet) {
    if (objet.getPlaqueCantonCode() == null) {
      return null;
    }
    return RipolLocation.builder()
        .code(objet.getPlaqueCantonCode())
        .label(objet.getPlaqueCantonLabel())
        .sourceTable(Ech051Constants.RipolSourceTables.PLAQUE_CANTON)
        .zipCode(null)
        .build();
  }

  /**
   * Construit la référence RIPOL pour le type d'objet.
   */
  public RipolReference buildObjectTypeReference(ObjetIncident objet) {
    if (objet.getTypeCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getTypeCode(), objet.getTypeLabel(), TYPE_OBJET);
  }

  /**
   * Construit la référence RIPOL pour la marque/fabricant d'un objet.
   */
  public RipolReference buildBrandReference(ObjetIncident objet) {
    if (objet.getFabricantCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getFabricantCode(), objet.getFabricantLabel(), OBJET_MARQUE);
  }

  /**
   * Construit la référence RIPOL pour le modèle d'un objet.
   */
  public RipolReference buildModelReference(ObjetIncident objet) {
    if (objet.getModeleCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getModeleCode(), objet.getModeleLabel(), OBJET_MODELE);
  }

  /**
   * Construit la référence RIPOL pour la couleur d'un objet.
   */
  public RipolReference buildColourReference(ObjetIncident objet) {
    if (objet.getCouleurCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getCouleurCode(), objet.getCouleurLabel(), OBJET_COULEUR);
  }

  /**
   * Construit la référence RIPOL pour la couleur secondaire d'un objet.
   */
  public RipolReference buildColourSecondaireReference(ObjetIncident objet) {
    if (objet.getCouleurSecondaireCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getCouleurSecondaireCode(), objet.getCouleurSecondaireLabel(), OBJET_COULEUR);
  }

  /**
   * Construit la référence RIPOL pour le type de véhicule.
   */
  public RipolReference buildVehicleTypeReference(ObjetIncident objet) {
    if (objet.getTypeCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getTypeCode(), objet.getTypeLabel(), TYPE_VEHICULE);
  }

  /**
   * Construit la référence RIPOL pour la couleur d'un véhicule.
   */
  public RipolReference buildVehicleColourReference(ObjetIncident objet) {
    if (objet.getCouleurCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getCouleurCode(), objet.getCouleurLabel(), COULEUR_VEHICULE);
  }

  /**
   * Construit la référence RIPOL pour la couleur secondaire d'un véhicule.
   */
  public RipolReference buildVehicleColourSecondaireReference(ObjetIncident objet) {
    if (objet.getCouleurSecondaireCode() == null) {
      return null;
    }
    return RipolReferenceBuilder.of(objet.getCouleurSecondaireCode(), objet.getCouleurSecondaireLabel(), COULEUR_VEHICULE);
  }

  /**
   * Construit l'identification d'un objet (IMEI ou numéro de série).
   */
  public Identification buildIdentification(ObjetIncident objet) {
    if (hasImeiForMyAbi(objet)) {
      return Identification.builder()
          .type("IMEI")
          .number(objet.getNumeroIMEI().trim())
          .build();
    }
    if (hasNumeroSerieForMyAbi(objet)) {
      return Identification.builder()
          .type("serialNumber")
          .number(objet.getNumeroSerie().trim())
          .build();
    }
    return null;
  }

  public String buildObjectAdditionalInfo(ObjetIncident objet) {
    List<String> details = MyAbiAdditionalInformationFormatter.builder();
    MyAbiAdditionalInformationFormatter.addLabeled(
        details, "Justification de l'absence de numéro IMEI", objet.getJustificationAbsenceIMEI());
    MyAbiAdditionalInformationFormatter.addBoolean(details, "Numéro de cadre inconnu", objet.isNumeroCadreInconnu());
    MyAbiAdditionalInformationFormatter.addBoolean(details, "Numéro de châssis (VIN) inconnu", objet.isVinInconnu());
    MyAbiAdditionalInformationFormatter.addBoolean(details, "Numéro de série inconnu", objet.isNumeroSerieInconnu());
    MyAbiAdditionalInformationFormatter.addBoolean(details, "Numéro IMEI inconnu", objet.isNumeroIMEIInconnu());
    addPlaqueNumeroForMyAbi(details, objet);
    return MyAbiAdditionalInformationFormatter.format(details);
  }

  /**
   * Construit les informations additionnelles d'un véhicule.
   */
  public String buildVehicleAdditionalInfo(ObjetIncident objet) {
    return buildObjectAdditionalInfo(objet);
  }

  private void addPlaqueNumeroForMyAbi(List<String> details, ObjetIncident objet) {
    if (!objet.isVehicleType() && hasPlaqueNumeroForMyAbi(objet)) {
      MyAbiAdditionalInformationFormatter.addLabeled(details, "Numéro de plaque", objet.getPlaqueNumero());
    }
  }

  private static String numeroSerieForMyAbi(ObjetIncident objet) {
    return hasNumeroSerieForMyAbi(objet) ? objet.getNumeroSerie().trim() : null;
  }

  private static String gravureForMyAbi(ObjetIncident objet) {
    return hasGravureForMyAbi(objet) ? objet.getGravure().trim() : null;
  }

  private static boolean hasNumeroSerieForMyAbi(ObjetIncident objet) {
    return !objet.isNumeroSerieInconnu()
        && objet.getNumeroSerie() != null
        && !objet.getNumeroSerie().isBlank();
  }

  private static boolean hasGravureForMyAbi(ObjetIncident objet) {
    return objet.getGravure() != null && !objet.getGravure().isBlank();
  }

  private static boolean hasImeiForMyAbi(ObjetIncident objet) {
    return !objet.isNumeroIMEIInconnu()
        && objet.getNumeroIMEI() != null
        && !objet.getNumeroIMEI().isBlank();
  }

  private static boolean hasPlaqueNumeroForMyAbi(ObjetIncident objet) {
    return !objet.isPlaqueInconnu()
        && objet.getPlaqueNumero() != null
        && !objet.getPlaqueNumero().isBlank();
  }

  private static String objectXmlKey(int index, int total) {
    if (total <= 1) {
      return Ech051Constants.OBJECT_KEY_TIERS;
    }
    if (index == 0) {
      return Ech051Constants.OBJECT_KEY_TIERS;
    }
    return Integer.toString(OBJECT_KEY_INDEX_OFFSET  + index);
  }

  private static String vehicleXmlKey(int index, int total) {
    if (total <= 1) {
      return Ech051Constants.VEHICLE_KEY;
    }
    if (index == 0) {
      return Ech051Constants.VEHICLE_KEY;
    }
    return Integer.toString(VEHICLE_KEY_INDEX_OFFSET  + index);
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private boolean isObjectEligibleForMyAbi(ObjetIncident objet) {
    String categorie = objet.getCategorieObjet();
    if (CATEGORIE_DOCUMENTS.equals(categorie) || CATEGORIE_PLAQUE.equals(categorie)) {
      return true;
    }
    if (objet.isVehicleType()) {
      return false;
    }
    return hasIdentificationForMyAbi(objet);
  }

  private boolean hasIdentificationForMyAbi(ObjetIncident objet) {
    return hasImeiForMyAbi(objet) || hasNumeroSerieForMyAbi(objet) || hasGravureForMyAbi(objet);
  }

  private boolean hasVehicleIdentificationForMyAbi(ObjetIncident objet) {
    if (!objet.isVinInconnu() && objet.getVin() != null && !objet.getVin().isBlank()) {
      return true;
    }
    if (!objet.isNumeroCadreInconnu() && objet.getNumeroCadre() != null && !objet.getNumeroCadre().isBlank()) {
      return true;
    }
    if (objet.getVelofinderId() != null && !objet.getVelofinderId().isBlank()) {
      return true;
    }
    if (hasPlaqueNumeroForMyAbi(objet)) {
      return true;
    }
    return hasIdentificationForMyAbi(objet);
  }
}
