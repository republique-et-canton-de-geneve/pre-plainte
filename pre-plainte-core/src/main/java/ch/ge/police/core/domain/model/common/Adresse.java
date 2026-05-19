package ch.ge.police.core.domain.model.common;


public record Adresse(
   String adresse,
   String adressePostale,
   String npa,
   String localite,
   String localiteCode,
   String pays,
   String paysCode
){}
