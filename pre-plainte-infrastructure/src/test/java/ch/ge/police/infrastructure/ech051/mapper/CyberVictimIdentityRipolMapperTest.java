package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.informationspersonnelles.common.TitreSejour;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CyberVictimIdentityRipolMapperTest {

  @Test
  void mapIdentificationType_returnsNullWhenTypeIsNull() {
    assertNull(CyberVictimIdentityRipolMapper.mapIdentificationType(null));
  }

  @Test
  void mapIdentificationType_returnsNullForStolenOrLostDocuments() {
    assertNull(CyberVictimIdentityRipolMapper.mapIdentificationType(TypeDocumentIdentite.DOCUMENTS_VOLES_PERDUS));
  }

  @Test
  void mapIdentificationType_mapsCarteIdentiteToIdPass() {
    assertEquals("IDPass", CyberVictimIdentityRipolMapper.mapIdentificationType(TypeDocumentIdentite.CARTE_IDENTITE));
  }

  @Test
  void mapIdentificationType_mapsPasseportToPassport() {
    assertEquals("Passport", CyberVictimIdentityRipolMapper.mapIdentificationType(TypeDocumentIdentite.PASSEPORT));
  }

  @Test
  void mapPermitCategory_returnsNullWhenTitreSejourIsNull() {
    assertNull(CyberVictimIdentityRipolMapper.mapPermitCategory(null));
  }

  @ParameterizedTest
  @EnumSource(value = TitreSejour.class, names = {"AUCUN", "SANS_PERMIS"})
  void mapPermitCategory_returnsNullForAbsentPermit(TitreSejour titreSejour) {
    assertNull(CyberVictimIdentityRipolMapper.mapPermitCategory(titreSejour));
  }

  @ParameterizedTest
  @MethodSource("permitCategoryMappings")
  void mapPermitCategory_mapsTitreSejourToRipolReference(
      TitreSejour titreSejour,
      String expectedCode,
      String expectedLabel
  ) {
    RipolReference reference = CyberVictimIdentityRipolMapper.mapPermitCategory(titreSejour);

    assertEquals(expectedCode, reference.getCode());
    assertEquals(expectedLabel, reference.getLabel());
    assertEquals(Ech051Constants.RipolSourceTables.PKS_AUFENTHALT, reference.getSourceTable());
    assertEquals("RIPOL", reference.getSource());
  }

  private static Stream<Arguments> permitCategoryMappings() {
    return Stream.of(
        Arguments.of(TitreSejour.PERMIS_B, "02", "B Permis de séjour"),
        Arguments.of(TitreSejour.PERMIS_B_REFUGIE, "02", "B Permis de séjour"),
        Arguments.of(TitreSejour.PERMIS_C, "03", "C Permis d'établissement"),
        Arguments.of(TitreSejour.PERMIS_CI, "01", "CI Permis avec activité lucrative"),
        Arguments.of(TitreSejour.PERMIS_L, "04", "L Autorisation de courte durée"),
        Arguments.of(TitreSejour.PERMIS_F_REFUGIE, "05", "F Admission provisoire"),
        Arguments.of(TitreSejour.PERMIS_F_PROVISOIRE, "05", "F Admission provisoire"),
        Arguments.of(TitreSejour.PERMIS_G, "08", "G Autorisation frontalière"),
        Arguments.of(TitreSejour.PERMIS_N, "06", "N Demande d'asile"),
        Arguments.of(TitreSejour.PERMIS_S, "07", "S Protection provisoire"),
        Arguments.of(TitreSejour.CARTE_LEGITIMATION, "09", "Carte de légitimation")
    );
  }
}
