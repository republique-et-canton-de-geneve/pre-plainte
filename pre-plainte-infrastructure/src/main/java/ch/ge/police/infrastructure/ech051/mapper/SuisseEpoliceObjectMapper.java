package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Identification;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.NumberPlate;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import org.springframework.stereotype.Component;

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

  private List<ObjectItem> buildObjectsFromVol(Vol vol) {
    if (vol.getObjetsVoles() == null) {
      return List.of();
    }

    List<ObjetIncident> nonVehicles = vol.getObjetsVoles().stream()
        .filter(objet -> !objet.isVehicleType())
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
        .numeroSerie(objet.getNumeroSerie())
        .gravure(objet.getGravure())
        .identification(buildIdentification(objet))
        .additionalInformation(buildObjectAdditionalInfo(objet))
        .build();
  }

  private List<ObjectItem> buildObjectsFromDommage(DommageMateriel dommage) {
    if (dommage.getObjetDegrades() != null && !dommage.getObjetDegrades().isEmpty()) {
      List<ObjetIncident> nonVehicles = dommage.getObjetDegrades().stream()
          .filter(objet -> !objet.isVehicleType())
          .toList();
      if (!nonVehicles.isEmpty()) {
        int n = nonVehicles.size();
        return IntStream.range(0, n)
            .mapToObj(i -> buildObjectItem(nonVehicles.get(i), objectXmlKey(i, n)))
            .toList();
      }
    }

    ObjectItem item = ObjectItem.builder()
        .key(Ech051Constants.OBJECT_KEY_TIERS)
        .typeOfObject(
            RipolReferenceBuilder.of(
                Ech051Constants.TYPE_OF_OBJECT_DOMMAGE_CODE,
                Ech051Constants.TYPE_OF_OBJECT_DOMMAGE_LABEL,
                TYPE_OBJET
            )
        )
        .additionalInformation(dommage.getDescription())
        .build();

    return List.of(item);
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
    if (objet.getPlaqueNumero() == null || objet.getPlaqueNumero().isBlank()) {
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
    if (objet.getNumeroIMEI() != null) {
      return Identification.builder()
          .type("IMEI")
          .number(objet.getNumeroIMEI())
          .build();
    }
    if (objet.getNumeroSerie() != null) {
      return Identification.builder()
          .type("serialNumber")
          .number(objet.getNumeroSerie())
          .build();
    }
    return null;
  }

  /**
   * Construit les informations additionnelles d'un objet.
   * Chaîne la description avec les justifications et informations complémentaires.
   */
  public String buildObjectAdditionalInfo(ObjetIncident objet) {
    List<String> details = new ArrayList<>();
    addImeiDetails(details, objet);
    addIfTrue(details, objet.isNumeroSerieInconnu(), "Numéro de série inconnu");
    addIfTrue(details, objet.isNumeroCadreInconnu(), "Numéro de cadre inconnu");
    addIfTrue(details, objet.isVinInconnu(), "VIN inconnu");
    addIfTrue(details, objet.isPlaqueInconnu(), "Numéro de plaque inconnu");
    addPlaqueNumero(details, objet);

    return details.isEmpty() ? null : String.join(" | ", details);
  }

  /**
   * Construit les informations additionnelles d'un véhicule.
   */
  public String buildVehicleAdditionalInfo(ObjetIncident objet) {
    return buildObjectAdditionalInfo(objet);
  }

  private void addIfTrue(List<String> details, boolean condition, String value) {
    if (condition) {
      details.add(value);
    }
  }

  private void addImeiDetails(List<String> details, ObjetIncident objet) {
    if (objet.isNumeroIMEIInconnu()) {
      String justification = objet.getJustificationAbsenceIMEI();
      if (justification != null && !justification.isBlank()) {
        details.add("IMEI inconnu: " + justification);
      }
    }
  }

  private void addPlaqueNumero(List<String> details, ObjetIncident objet) {
    if (objet.getPlaqueNumero() != null && !objet.getPlaqueNumero().isBlank() && !objet.isVehicleType()) {
      details.add("Numéro de plaque: " + objet.getPlaqueNumero());
    }
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
}
