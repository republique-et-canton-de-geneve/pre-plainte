package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.informationspersonnelles.common.TitreSejour;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.PKS_AUFENTHALT;

/**
 * Codes RIPOL pour l'objet pièce d'identité du lésé (cybercrime achat non reçu).
 */
public final class CyberVictimIdentityRipolMapper {

  private static final String IDENTIFICATION_TYPE_ID_PASS = "IDPass";
  private static final String IDENTIFICATION_TYPE_PASSPORT = "Passport";

  private CyberVictimIdentityRipolMapper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String mapIdentificationType(TypeDocumentIdentite typeDocument) {
    if (typeDocument == null || typeDocument == TypeDocumentIdentite.DOCUMENTS_VOLES_PERDUS) {
      return null;
    }
    return switch (typeDocument) {
      case CARTE_IDENTITE -> IDENTIFICATION_TYPE_ID_PASS;
      case PASSEPORT -> IDENTIFICATION_TYPE_PASSPORT;
      default -> null;
    };
  }

  public static RipolReference mapPermitCategory(TitreSejour titreSejour) {
    if (titreSejour == null || titreSejour == TitreSejour.AUCUN || titreSejour == TitreSejour.SANS_PERMIS) {
      return null;
    }
    return switch (titreSejour) {
      case PERMIS_B, PERMIS_B_REFUGIE -> RipolReferenceBuilder.of("02", "B Permis de séjour", PKS_AUFENTHALT);
      case PERMIS_C -> RipolReferenceBuilder.of("03", "C Permis d'établissement", PKS_AUFENTHALT);
      case PERMIS_CI -> RipolReferenceBuilder.of("01", "CI Permis avec activité lucrative", PKS_AUFENTHALT);
      case PERMIS_L -> RipolReferenceBuilder.of("04", "L Autorisation de courte durée", PKS_AUFENTHALT);
      case PERMIS_F_REFUGIE, PERMIS_F_PROVISOIRE ->
          RipolReferenceBuilder.of("05", "F Admission provisoire", PKS_AUFENTHALT);
      case PERMIS_G -> RipolReferenceBuilder.of("08", "G Autorisation frontalière", PKS_AUFENTHALT);
      case PERMIS_N -> RipolReferenceBuilder.of("06", "N Demande d'asile", PKS_AUFENTHALT);
      case PERMIS_S -> RipolReferenceBuilder.of("07", "S Protection provisoire", PKS_AUFENTHALT);
      case CARTE_LEGITIMATION -> RipolReferenceBuilder.of("09", "Carte de légitimation", PKS_AUFENTHALT);
      default -> null;
    };
  }
}
