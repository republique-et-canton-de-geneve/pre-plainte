package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuisseEpoliceAddressMapperTest {

  private SuisseEpoliceAddressMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new SuisseEpoliceAddressMapper();
  }

  @Test
  void fromAdresse_returnsNullWhenNull() {
    assertNull(mapper.fromAdresse(null));
  }

  @Test
  void fromAdresse_usesAdresseAsStreet() {
    Adresse a = new Adresse("Rue A 1", "", "1200", "Genève", "1200", "Suisse", "8100");
    Address dto = mapper.fromAdresse(a);
    assertNotNull(dto);
    assertEquals("Rue A 1", dto.getStreet());
    assertNull(dto.getHouseNumber());
    assertNotNull(dto.getPlace());
    assertEquals("Genève", dto.getPlace().getLabel());
    assertEquals("1200", dto.getPlace().getZipCode());
    assertNotNull(dto.getCountry());
    assertEquals("Suisse", dto.getCountry().getLabel());
  }

  @Test
  void fromAdresse_fallsBackToAdressePostaleWhenAdresseBlank() {
    Adresse a = new Adresse("  ", "Poste 5", "1200", "Genève", "1200", "Suisse", "8100");
    Address dto = mapper.fromAdresse(a);
    assertEquals("Poste 5", dto.getStreet());
    assertNull(dto.getAdditional());
  }

  @Test
  void fromAdresse_setsAdditionalWhenPostaleDiffersFromStreet() {
    Adresse a = new Adresse("Rue X", "Boîte 12", "1200", "Genève", "1200", "Suisse", "8100");
    Address dto = mapper.fromAdresse(a);
    assertEquals("Rue X", dto.getStreet());
    assertEquals("Boîte 12", dto.getAdditional());
  }

  @Test
  void fromAdresse_noAdditionalWhenPostaleEqualsStreet() {
    Adresse a = new Adresse("Rue Y", "Rue Y", "1200", "Genève", "1200", "Suisse", "8100");
    Address dto = mapper.fromAdresse(a);
    assertEquals("Rue Y", dto.getStreet());
    assertNull(dto.getAdditional());
  }

  @Test
  void buildAddressPlace_returnsNullWhenAdresseNull() {
    assertNull(mapper.buildAddressPlace(null));
  }

  @Test
  void buildAddressCountry_returnsNullWhenAdresseNull() {
    assertNull(mapper.buildAddressCountry(null));
  }

  @Test
  void buildAddressPlace_returnsNullWhenNoLocaliteNorNpa() {
    Adresse a = new Adresse("r", "", "", "", null, "", null);
    assertNull(mapper.buildAddressPlace(a));
  }

  @Test
  void buildAddressPlace_prefersLocaliteWithCodeAndNpa() {
    Adresse a = new Adresse("r", "", "1200", "Genève", "1201", "CH", "8100");
    RipolLocation p = mapper.buildAddressPlace(a);
    assertNotNull(p);
    assertEquals("1201", p.getCode());
    assertEquals("Genève", p.getLabel());
    assertEquals("PTT_ORT", p.getSourceTable());
    assertEquals("1200", p.getZipCode());
  }

  @Test
  void buildAddressPlace_npaOnlyUsesNpaAsLabelAndZip() {
    Adresse a = new Adresse("", "", "1200", "", null, "", null);
    RipolLocation p = mapper.buildAddressPlace(a);
    assertNotNull(p);
    assertNull(p.getCode());
    assertEquals("1200", p.getLabel());
    assertEquals("1200", p.getZipCode());
  }

  @Test
  void buildAddressCountry_returnsNullWhenNoPays() {
    Adresse a = new Adresse("r", "", "1200", "Ville", null, "", null);
    assertNull(mapper.buildAddressCountry(a));
  }

  @Test
  void buildAddressCountry_codeOnlyUsesCodeAsLabelFallback() {
    Adresse a = new Adresse("r", "", "1200", "Ville", null, "", "8100");
    RipolLocation c = mapper.buildAddressCountry(a);
    assertNotNull(c);
    assertEquals("8100", c.getCode());
    assertEquals("8100", c.getLabel());
    assertEquals("EXT_GPNATI", c.getSourceTable());
  }

  @Test
  void buildAddressCountry_labelAndCode() {
    Adresse a = new Adresse("r", "", "1200", "Ville", null, "Suisse", "8100");
    RipolLocation c = mapper.buildAddressCountry(a);
    assertEquals("8100", c.getCode());
    assertEquals("Suisse", c.getLabel());
  }

  @Test
  void isAddressComplete_falseForNull() {
    assertFalse(mapper.isAddressComplete(null));
  }

  @Test
  void isAddressComplete_trueWhenAnyPartPresent() {
    assertTrue(mapper.isAddressComplete(new Adresse("x", "", "", "", null, "", null)));
    assertTrue(mapper.isAddressComplete(new Adresse("", "y", "", "", null, "", null)));
    assertTrue(mapper.isAddressComplete(new Adresse("", "", "z", "", null, "", null)));
    assertTrue(mapper.isAddressComplete(new Adresse("", "", "", "L", null, "", null)));
  }

  @Test
  void isAddressComplete_falseWhenAllBlank() {
    assertFalse(mapper.isAddressComplete(new Adresse("", "", "", "", null, "", null)));
  }
}
