package ch.ge.police.core.domain.model.common;


import static ch.ge.police.core.domain.util.validation.ChampValidator.verifierLongueurMax;
import static ch.ge.police.core.domain.util.validation.ValidationConstants.TEXT_FIELD_MAX_LENGTH;

public record Adresse(
   String adresse,
   String adressePostale,
   String npa,
   String localite,
   String localiteCode,
   String pays,
   String paysCode
){
  public void validate() {
    verifierLongueurMax(adresse, TEXT_FIELD_MAX_LENGTH, "adresse");
    verifierLongueurMax(adressePostale, TEXT_FIELD_MAX_LENGTH, "adressePostale");
    verifierLongueurMax(npa, TEXT_FIELD_MAX_LENGTH, "npa");
    verifierLongueurMax(localite, TEXT_FIELD_MAX_LENGTH, "localite");
    verifierLongueurMax(localiteCode, TEXT_FIELD_MAX_LENGTH, "localiteCode");
    verifierLongueurMax(pays, TEXT_FIELD_MAX_LENGTH, "pays");
    verifierLongueurMax(paysCode, TEXT_FIELD_MAX_LENGTH, "paysCode");
  }
}
