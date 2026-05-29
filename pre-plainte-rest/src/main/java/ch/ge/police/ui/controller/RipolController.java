package ch.ge.police.ui.controller;

import ch.ge.police.core.domain.model.ripol.Ripol;
import ch.ge.police.core.port.out.RipolPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/ripol")
@RequiredArgsConstructor
public class RipolController {

  public static final String CODE_TELEPHONE_MOBILE = "713103";
  public static final String MASTER_TYPE_OBJETS = "183";
  public static final String MASTER_TYPE_VEHICULES = "101";
  public static final String TBINCIDENTCODE = "TBINCIDENTCODE";
  public static final String GROUP_TYPE_SEXE = "geschlechtISO";
  public static final String GROUP_TYPE_NATIONALITE = "11";
  public static final String GROUP_TYPE_COMMUNE = "272";
  public static final String GROUP_TYPE_LIEU_ORIGINE = "271";
  public static final String GROUP_TYPE_TYPE_DOCUMENT = "01M";
  public static final String GROUP_TYPE_LIEU = "390";
  public static final String GROUP_TYPE_MODE_OPERATOIRE = "kasPhaenomen";
  public static final String GROUP_TYPE_COULEUR = "103";
  public static final String GROUP_TYPE_CANTON = "1";
  public static final String GROUP_TYPE_VEHICLE_BRAND = "102";
  public static final String MASTER_TYPE_VEHICLE_MODEL = GROUP_TYPE_VEHICLE_BRAND;
  public static final String GROUP_TYPE_VEHICLE_INSURER = "185";
  private static final String GROUP_TYPE_VEHICLE_INSURER_ALT = "186";

  private static final List<String> VEHICLE_INSURER_GROUP_TYPES = List.of(
      GROUP_TYPE_VEHICLE_INSURER,
      GROUP_TYPE_VEHICLE_INSURER_ALT
  );

  private static final List<String> KNOWN_VEHICLE_INSURER_NAMES = List.of(
      "AXA", "ALLIANCE", "ALLIANZ", "MOBILIAR", "GENERALI", "ZURICH", "HELVETIA", "BASLER", "VAUDOISE", "BALOISE",
      "SWISS", "EMMENTAL", "COOP", "SMILE", "BONUS", "PROGRES", "ORION"
  );

  private static final List<Ripol> DEFAULT_VEHICLE_INSURERS = List.of(
      ripolInsurer("AXA", "AXA"),
      ripolInsurer("ALLIANCE", "Alliance"),
      ripolInsurer("ALLIANZ", "Allianz"),
      ripolInsurer("MOBILIAR", "Mobiliar"),
      ripolInsurer("GENERALI", "Generali"),
      ripolInsurer("ZURICH", "Zurich"),
      ripolInsurer("HELVETIA", "Helvetia"),
      ripolInsurer("BASLER", "Basler"),
      ripolInsurer("VAUDOISE", "Vaudoise"),
      ripolInsurer("BALOISE", "Baloise")
  );

  private final RipolPort ripolPort;

  @GetMapping("/codes")
  public List<Ripol> getCodes(
      @RequestParam String groupType,
      @RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(groupType, search);
    }
    return ripolPort.getCodesByGroupType(groupType);
  }

  @GetMapping("/group-types")
  public List<String> getGroupTypes() {
    return ripolPort.listDistinctGroupTypes(TBINCIDENTCODE);
  }

  @GetMapping("/brands")
  public List<Ripol> getBrands(
      @RequestParam String masterValue,
      @RequestParam(required = false, defaultValue = "183") String masterType,
      @RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchBrands(masterValue, masterType, search);
    }
    return ripolPort.getBrandsByTypeAndMasterType(masterValue, masterType);
  }

  @GetMapping("/models")
  public List<Ripol> getModels(
      @RequestParam String masterValue,
      @RequestParam(required = false) String search) {
    return getModelRipols(masterValue, search);
  }

  @GetMapping("/object-types")
  public List<Ripol> getObjectTypes(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(MASTER_TYPE_OBJETS, search);
    }
    return ripolPort.getCodesByGroupType(MASTER_TYPE_OBJETS);
  }

  @GetMapping("/phone-brands")
  public List<Ripol> getPhoneBrands(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchBrands(CODE_TELEPHONE_MOBILE, MASTER_TYPE_OBJETS, search);
    }
    return ripolPort.getBrandsByType(CODE_TELEPHONE_MOBILE);
  }

  @GetMapping("/phone-models")
  public List<Ripol> getPhoneModels(
      @RequestParam String brandCode,
      @RequestParam(required = false) String search) {
    return getModelRipols(brandCode, search);
  }

  @GetMapping("/vehicle-types")
  public List<Ripol> getVehicleTypes(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(MASTER_TYPE_VEHICULES, search);
    }
    return ripolPort.getCodesByGroupType(MASTER_TYPE_VEHICULES);
  }

  @GetMapping("/vehicle-brands")
  public List<Ripol> getVehicleBrands(
      @RequestParam String vehicleTypeCode,
      @RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_VEHICLE_BRAND, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_VEHICLE_BRAND);
  }

  @GetMapping("/vehicle-models")
  public List<Ripol> getVehicleModels(
      @RequestParam String brandCode,
      @RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchBrands(brandCode, MASTER_TYPE_VEHICLE_MODEL, search);
    }
    return ripolPort.getBrandsByTypeAndMasterType(brandCode, MASTER_TYPE_VEHICLE_MODEL);
  }

  @GetMapping("/sexes")
  public List<Ripol> getSexes() {
    return ripolPort.getCodesByGroupType(GROUP_TYPE_SEXE);
  }

  @GetMapping("/nationalities")
  public List<Ripol> getNationalities(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_NATIONALITE, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_NATIONALITE);
  }

  @GetMapping("/communes")
  public List<Ripol> getCommunes(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_COMMUNE, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_COMMUNE);
  }

  @GetMapping("/lieux-origine")
  public List<Ripol> getLieuxOrigine(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_LIEU_ORIGINE, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_LIEU_ORIGINE);
  }

  @GetMapping("/document-types")
  public List<Ripol> getDocumentTypes(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_TYPE_DOCUMENT, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_TYPE_DOCUMENT);
  }

  @GetMapping("/location-types")
  public List<Ripol> getLocationTypes(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_LIEU, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_LIEU);
  }

  @GetMapping("/modus-operandi")
  public List<Ripol> getModusOperandi(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_MODE_OPERATOIRE, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_MODE_OPERATOIRE);
  }

  @GetMapping("/object-colours")
  public List<Ripol> getObjectColours(@RequestParam(required = false) String search) {
    return getColourRipols(search);
  }

  @GetMapping("/vehicle-colours")
  public List<Ripol> getVehicleColours(@RequestParam(required = false) String search) {
    return getColourRipols(search);
  }

  @GetMapping("/vehicle-insurers")
  public List<Ripol> getVehicleInsurers(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return searchVehicleInsurers(search.trim());
    }
    return loadDefaultVehicleInsurers();
  }

  private List<Ripol> loadDefaultVehicleInsurers() {
    Map<String, Ripol> byCode = new LinkedHashMap<>();
    for (String insurerName : KNOWN_VEHICLE_INSURER_NAMES) {
      for (String groupType : VEHICLE_INSURER_GROUP_TYPES) {
        for (Ripol ripol : ripolPort.searchCodesByGroupType(groupType, insurerName)) {
          if (isKnownVehicleInsurerName(ripol)) {
            byCode.putIfAbsent(ripol.code(), ripol);
          }
        }
      }
    }
    if (byCode.isEmpty()) {
      return DEFAULT_VEHICLE_INSURERS;
    }
    return byCode.values().stream()
        .sorted(Comparator.comparing(Ripol::labelFr, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  private List<Ripol> searchVehicleInsurers(String search) {
    Map<String, Ripol> byCode = new LinkedHashMap<>();
    String searchUpper = search.toUpperCase(Locale.ROOT);

    for (String groupType : VEHICLE_INSURER_GROUP_TYPES) {
      for (Ripol ripol : ripolPort.searchCodesByGroupType(groupType, search)) {
        if (matchesVehicleInsurerSearch(ripol, searchUpper)) {
          byCode.putIfAbsent(ripol.code(), ripol);
        }
      }
    }

    for (Ripol fallback : DEFAULT_VEHICLE_INSURERS) {
      if (matchesVehicleInsurerSearch(fallback, searchUpper)) {
        byCode.putIfAbsent(fallback.code(), fallback);
      }
    }

    if (byCode.isEmpty()) {
      return DEFAULT_VEHICLE_INSURERS.stream()
          .filter(r -> matchesVehicleInsurerSearch(r, searchUpper))
          .toList();
    }

    return byCode.values().stream()
        .sorted(Comparator.comparing(Ripol::labelFr, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  private boolean matchesVehicleInsurerSearch(Ripol ripol, String searchUpper) {
    if (ripol == null || searchUpper.isBlank()) {
      return false;
    }
    String label = resolveInsurerLabel(ripol).toUpperCase(Locale.ROOT);
    return label.contains(searchUpper) || searchUpper.contains(label);
  }

  private boolean isKnownVehicleInsurerName(Ripol ripol) {
    if (ripol == null) {
      return false;
    }
    String label = resolveInsurerLabel(ripol).toUpperCase(Locale.ROOT);
    if (label.isBlank()) {
      return false;
    }
    return KNOWN_VEHICLE_INSURER_NAMES.stream()
        .anyMatch(known -> label.equals(known) || label.contains(known));
  }

  private static String resolveInsurerLabel(Ripol ripol) {
    if (ripol.labelFr() != null && !ripol.labelFr().isBlank()) {
      return ripol.labelFr().trim();
    }
    if (ripol.labelDe() != null && !ripol.labelDe().isBlank()) {
      return ripol.labelDe().trim();
    }
    return "";
  }

  private static Ripol ripolInsurer(String code, String label) {
    return new Ripol(code, label, label, GROUP_TYPE_VEHICLE_INSURER);
  }

  @GetMapping("/cantons")
  public List<Ripol> getCantons(@RequestParam(required = false) String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_CANTON, search);
    } else {
      return ripolPort.getCodesByGroupType(GROUP_TYPE_CANTON);
    }
  }

  private List<Ripol> getModelRipols(String brandCode, String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchModels(brandCode, search);
    }
    return ripolPort.getModelsByBrand(brandCode);
  }

  private List<Ripol> getColourRipols(String search) {
    if (search != null && !search.isBlank()) {
      return ripolPort.searchCodesByGroupType(GROUP_TYPE_COULEUR, search);
    }
    return ripolPort.getCodesByGroupType(GROUP_TYPE_COULEUR);
  }
}
