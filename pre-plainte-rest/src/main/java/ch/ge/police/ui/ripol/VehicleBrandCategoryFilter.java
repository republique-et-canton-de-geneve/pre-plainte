package ch.ge.police.ui.ripol;

import ch.ge.police.core.domain.model.ripol.Ripol;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class VehicleBrandCategoryFilter {

  private static final Set<String> KNOWN_CAR_MANUFACTURERS = Set.of(
      "AUDI", "BMW", "MERCEDES", "MERCEDES-BENZ", "VOLKSWAGEN", "VW", "FORD", "TOYOTA", "HONDA",
      "PEUGEOT", "RENAULT", "CITROEN", "OPEL", "FIAT", "PORSCHE", "SEAT", "SKODA", "VOLVO",
      "MAZDA", "NISSAN", "HYUNDAI", "KIA", "LEXUS", "JAGUAR", "LAND ROVER", "MINI", "SMART",
      "ALFA ROMEO", "JEEP", "CHRYSLER", "DACIA", "SUZUKI", "SUBARU", "MITSUBISHI", "TESLA",
      "LAMBORGHINI", "FERRARI", "MASERATI", "BENTLEY", "ROLLS-ROYCE", "ASTON MARTIN"
  );

  private VehicleBrandCategoryFilter() {}

  public static List<Ripol> filterByVehicleTypeCode(List<Ripol> brands, String vehicleTypeCode) {
    if (vehicleTypeCode == null || vehicleTypeCode.isBlank()) {
      return brands;
    }
    return brands.stream()
        .filter(brand -> matchesVehicleTypeCategory(brand, vehicleTypeCode))
        .toList();
  }

  private static boolean matchesVehicleTypeCategory(Ripol ripol, String vehicleTypeCode) {
    String label = displayLabel(ripol);
    if (label.isBlank()) {
      return false;
    }
    if (isMotorVehicleTypeCode(vehicleTypeCode)) {
      return !isBikeBrandLabel(label);
    }
    if (isBikeVehicleTypeCode(vehicleTypeCode)) {
      if (isBikeBrandLabel(label)) {
        return true;
      }
      return !isKnownCarManufacturer(label);
    }
    return true;
  }

  private static String displayLabel(Ripol ripol) {
    if (ripol.labelFr() != null && !ripol.labelFr().isBlank()) {
      return ripol.labelFr().trim();
    }
    if (ripol.labelDe() != null && !ripol.labelDe().isBlank()) {
      return ripol.labelDe().trim();
    }
    return "";
  }

  private static boolean isMotorVehicleTypeCode(String code) {
    if (code == null || code.length() < 2) {
      return false;
    }
    return code.startsWith("010")
        || code.startsWith("011")
        || code.startsWith("020")
        || code.startsWith("030")
        || code.startsWith("031")
        || code.startsWith("032")
        || code.startsWith("060")
        || code.startsWith("061")
        || code.startsWith("062")
        || code.startsWith("064")
        || code.startsWith("070");
  }

  private static boolean isBikeVehicleTypeCode(String code) {
    return code != null && (code.startsWith("200") || code.startsWith("201"));
  }

  private static boolean isBikeBrandLabel(String label) {
    String upper = label.toUpperCase(Locale.ROOT);
    if (upper.contains("BIKE") || upper.contains("BICYCLE") || upper.contains("FAHRRAD")) {
      return true;
    }
    if (upper.contains("VELO") && !upper.contains("VOLVO") && !upper.contains("CARAVELLE")) {
      return true;
    }
    return false;
  }

  private static boolean isKnownCarManufacturer(String label) {
    String upper = label.toUpperCase(Locale.ROOT);
    return KNOWN_CAR_MANUFACTURERS.stream().anyMatch(make -> labelMatchesManufacturer(upper, make));
  }

  private static boolean labelMatchesManufacturer(String upperLabel, String manufacturer) {
    return upperLabel.equals(manufacturer)
        || upperLabel.startsWith(manufacturer + " ")
        || upperLabel.endsWith(" " + manufacturer)
        || upperLabel.contains(" " + manufacturer + " ");
  }
}
