package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Address;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import org.springframework.stereotype.Component;

@Component
public class SuisseEpoliceAddressMapper {

  public Address fromAdresse(Adresse adresse) {
    if (adresse == null) {
      return null;
    }
    String streetLine = resolveStreetLineForXml(adresse);
    return Address.builder()
        .street(streetLine)
        .houseNumber(null)
        .place(buildAddressPlace(adresse))
        .country(buildAddressCountry(adresse))
        .additional(resolveAdditionalLineForXml(adresse, streetLine))
        .build();
  }

  private static String resolveStreetLineForXml(Adresse adresse) {
    if (adresse.adresse() != null && !adresse.adresse().isBlank()) {
      return adresse.adresse().strip();
    }
    if (adresse.adressePostale() != null && !adresse.adressePostale().isBlank()) {
      return adresse.adressePostale().strip();
    }
    return null;
  }

  private static String resolveAdditionalLineForXml(Adresse adresse, String streetLine) {
    if (adresse.adressePostale() == null || adresse.adressePostale().isBlank()) {
      return null;
    }
    String postale = adresse.adressePostale().strip();
    if (streetLine == null || streetLine.isBlank()) {
      return null;
    }
    if (postale.equals(streetLine)) {
      return null;
    }
    return postale;
  }

  public RipolLocation buildAddressPlace(Adresse adresse) {
    if (adresse == null) {
      return null;
    }
    if (adresse.localite() != null && !adresse.localite().isBlank()) {
      return RipolLocation.builder()
          .code(adresse.localiteCode())
          .label(adresse.localite().strip())
          .sourceTable("PTT_ORT")
          .zipCode(blankToNull(adresse.npa()))
          .build();
    }
    if (adresse.npa() != null && !adresse.npa().isBlank()) {
      String npa = adresse.npa().strip();
      return RipolLocation.builder()
          .code(null)
          .label(npa)
          .sourceTable("PTT_ORT")
          .zipCode(npa)
          .build();
    }
    return null;
  }

  public RipolLocation buildAddressCountry(Adresse adresse) {
    if (adresse == null) {
      return null;
    }

    String paysCode = blankToNull(adresse.paysCode());
    String paysLabel = adresse.pays() != null && !adresse.pays().isBlank() ? adresse.pays().strip() : null;

    if (paysCode == null && paysLabel == null) {
      return null;
    }

    return RipolLocation.builder()
        .code(paysCode)
        .label(paysLabel != null ? paysLabel : paysCode)
        .sourceTable("EXT_GPNATI")
        .zipCode(null)
        .build();
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.strip();
  }

  public boolean isAddressComplete(Adresse adresse) {
    return adresse != null
        && ((adresse.adresse() != null && !adresse.adresse().isBlank())
        || (adresse.adressePostale() != null && !adresse.adressePostale().isBlank())
        || (adresse.npa() != null && !adresse.npa().isBlank())
        || (adresse.localite() != null && !adresse.localite().isBlank()));
  }
}
