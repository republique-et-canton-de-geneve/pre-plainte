package ch.ge.police.ui.ripol;

import ch.ge.police.core.domain.model.ripol.Ripol;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleBrandCategoryFilterTest {

  private static Ripol brand(String code, String label) {
    return new Ripol(code, label, label, "102");
  }

  @Test
  void filterByVehicleTypeCode_shouldExcludeBikeBrandsForCarType() {
    List<Ripol> brands = List.of(
        brand("6604", "AUDI"),
        brand("7352", "3G BIKES"),
        brand("6590", "ACT-BIKES"));

    List<Ripol> filtered = VehicleBrandCategoryFilter.filterByVehicleTypeCode(brands, "010101");

    assertEquals(1, filtered.size());
    assertEquals("AUDI", filtered.getFirst().labelFr());
  }

  @Test
  void filterByVehicleTypeCode_shouldExcludeCarMakesForBikeType() {
    List<Ripol> brands = List.of(
        brand("6604", "AUDI"),
        brand("7352", "3G BIKES"));

    List<Ripol> filtered = VehicleBrandCategoryFilter.filterByVehicleTypeCode(brands, "200600");

    assertEquals(1, filtered.size());
    assertTrue(filtered.getFirst().labelFr().contains("BIKE"));
  }
}
