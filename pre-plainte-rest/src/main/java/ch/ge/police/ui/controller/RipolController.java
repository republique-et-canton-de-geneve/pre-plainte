package ch.ge.police.ui.controller;

import ch.ge.police.core.domain.model.ripol.Ripol;
import ch.ge.police.core.port.out.RipolPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
      return ripolPort.searchBrands(vehicleTypeCode, MASTER_TYPE_VEHICULES, search);
    }
    return ripolPort.getBrandsByTypeAndMasterType(vehicleTypeCode, MASTER_TYPE_VEHICULES);
  }

  @GetMapping("/vehicle-models")
  public List<Ripol> getVehicleModels(
      @RequestParam String brandCode,
      @RequestParam(required = false) String search) {
    return getModelRipols(brandCode, search);
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
