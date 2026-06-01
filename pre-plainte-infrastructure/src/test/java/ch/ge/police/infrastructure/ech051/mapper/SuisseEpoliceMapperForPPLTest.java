package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.cybercrime.common.MoyenPaiement;
import ch.ge.police.core.domain.model.event.cybercrime.common.PlateformeUtilisee;
import ch.ge.police.core.domain.model.event.cybercrime.common.TypeCybercrime;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.PersonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SuisseEpoliceMapperForPPLTest {

  private SuisseEpoliceMapperForPPL mapper;

  @BeforeEach
  void setUp() {
    SuisseEpoliceAddressMapper addressMapper = new SuisseEpoliceAddressMapper();
    mapper = new SuisseEpoliceMapperForPPL(
        new SuisseEpolicePersonMapper(addressMapper),
        new SuisseEpoliceEventMapper(addressMapper),
        new SuisseEpoliceObjectMapper(),
        new SuisseEpoliceRelationsMapper(),
        new SuisseEpoliceBusinessCaseMapper(),
        addressMapper
    );
  }

  @Test
  void toDocument_returnsNullWhenPrePlainteIsNull() {
    assertNull(mapper.toDocument(null));
  }

  @Test
  void toDocument_returnsPayloadWhenPrePlainteIsMinimal() {
    PrePlainte prePlainte = new PrePlainte();
    prePlainte.setDemandeId("D-1");

    Ech0051DocumentPayload doc = mapper.toDocument(prePlainte);

    assertThat(doc).isNotNull();
    assertThat(doc.getProcessData()).isNotNull();
  }

  private static InformationsPersonnelles basePersonne() {
    InformationsPersonnelles ip = new InformationsPersonnelles();
    ip.setNom("N");
    ip.setPrenom("P");
    ip.setGenre(new RipolCode("1", "M"));
    ip.setNationalite(new RipolCode("8100", "CH"));
    ip.setDateNaissance("1990-01-01");
    ip.setAdresse(new Adresse("Rue 1", "", "1200", "Genève", "1200", "Suisse", "8100"));
    ip.setTelephone("41790000000");
    ip.setEmail("a@b.ch");
    ip.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    ip.setNumeroDocumentIdentite("AB12");
    return ip;
  }

  @Test
  void toDocument_mapsVolWithPersonsEventsAndBusinessCase() {
    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01T10:00");
    vol.setDateFinEvent("2025-01-01T11:00");

    PrePlainte p = new PrePlainte("VOL-PPL", basePersonne(), Incident.of(vol));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getPersons()).isNotEmpty();
    assertThat(doc.getEvents()).hasSize(1);
    assertThat(doc.getBusinessCases()).hasSize(1);
    assertThat(doc.getBusinessCases().getFirst().getCaseNumber()).isEqualTo("VOL-PPL");
  }

  @Test
  void toDocument_mapsCyberAchatWithLegalEntityCounterparty() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("V");
    achat.setPrenomVendeur("W");
    achat.setAchatViaPlaceMarche(false);
    achat.setNomEntrepriseVendeur("ACME SA");
    achat.setPlateformeUtilisee(PlateformeUtilisee.ANIBIS);
    achat.setMoyenPaiement(MoyenPaiement.TWINT);
    achat.setDateOperation("2025-03-01");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");
    cyber.setDescriptionCybercrime("Achat jamais reçu");

    PrePlainte p = new PrePlainte("CYB-ENT", basePersonne(), Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getObjects()).isEmpty();
    assertThat(doc.getRelations().getEventObjectLinks()).isEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isEmpty();
    assertThat(doc.getPersons().stream().anyMatch(
        person -> person != null && person.getType() == PersonType.LEGAL)).isTrue();
  }

  @Test
  void toDocument_mapsCyberCommandeFrauduleuse_addsInsurerPersonWithoutObjects() {
    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setDateDecouverte("2025-03-01");
    cf.setMontant(250.0);

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");
    cyber.setDescriptionCybercrime("Commande frauduleuse");

    PrePlainte p = new PrePlainte("CYB-CF", basePersonne(), Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getObjects()).isEmpty();
    assertThat(doc.getRelations().getEventObjectLinks()).isEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isEmpty();
    assertThat(doc.getPersons().stream().map(Ech0051DocumentPayload.Person::getKey))
        .contains(Ech051Constants.INSURER_REF_CYBER);
  }

  @Test
  void toDocument_mapsCyberFausseAnnonce_naturalCounterparty() {
    FausseAnnonce f = new FausseAnnonce();
    f.setTitreAnnonce("Appartement");
    f.setNomBailleur("Martin");
    f.setEmailBailleur("m@x.ch");
    f.setTelephoneBailleur("41791234567");
    f.setAdresseBienImmobilier("Chemin des Pins 3");
    f.setMontantDemande(2200d);
    f.setModePaiementDemande("Virement");
    f.setUrlComplete("FAUSSE-1");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.FAUSSE_ANNONCE);
    cyber.setFausseAnnonce(f);
    cyber.setDateDebutEvent("2025-04-01");
    cyber.setDateFinEvent("2025-04-02");
    cyber.setDescriptionCybercrime("Arnaque au loyer");

    PrePlainte p = new PrePlainte("FAUSSE-PPL", basePersonne(), Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getObjects()).isEmpty();
    assertThat(doc.getRelations().getEventObjectLinks()).isEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isEmpty();
    assertThat(doc.getPersons().stream().filter(x -> x != null && x.getType() == PersonType.NATURAL).count()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void toDocument_mapsCyberAchat_naturalSellerViaMarketplace_withAddressAndVictimTechFlags() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Vendeur");
    achat.setPrenomVendeur("Victor");
    achat.setAchatViaPlaceMarche(true);
    achat.setEmailVendeur("v@v.ch");
    achat.setTelephoneVendeur("41790000001");
    achat.setAdresseVendeur(new Adresse("Rue du Marché 2", "", "1200", "Genève", "1200", "CH", "8100"));
    achat.setSiteWebEntrepriseVendeur("https://shop.example");
    achat.setPlateformeUtilisee(PlateformeUtilisee.RICARDO);
    achat.setMoyenPaiement(MoyenPaiement.PAYPAL);
    achat.setDateOperation("2025-05-10T14:30:00+02:00");
    achat.setMontantDelitAchatLigne("50");
    achat.setPlateformeId("ricardo-99");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2025-05-01");
    cyber.setDateFinEvent("2025-05-02");
    cyber.setDescriptionCybercrime("Colis vide");

    InformationsPersonnelles ip = basePersonne();
    ip.setNumeroDocumentIdentite("");

    PrePlainte p = new PrePlainte("ACHAT-NAT", ip, Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getObjects()).isEmpty();
    assertThat(doc.getRelations().getEventObjectLinks()).isEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isEmpty();
  }

  @Test
  void toDocument_mapsCyberAutre_skipsObjectsAndCounterparty() {
    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.AUTRE);
    cyber.setDateDebutEvent("2025-06-01");
    cyber.setDateFinEvent("2025-06-02");
    cyber.setDescriptionCybercrime("Autre infraction");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("CYB-AUTRE", basePersonne(), Incident.of(cyber)));

    assertThat(doc.getObjects()).isEmpty();
  }

  @Test
  void toDocument_commandeFrauduleuse_enrichesCounterpartyAdditionalInfo() {
    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setDateDecouverte("2025-07-01");
    cf.setMontant(99.5);
    cf.setAssurance(false);

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-07-01");
    cyber.setDateFinEvent("2025-07-01");
    cyber.setDescriptionCybercrime("Usurpation");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("CMD-INFOS", basePersonne(), Incident.of(cyber)));

    assertThat(doc.getPersons().stream().anyMatch(
        p -> p != null
            && p.getAdditionalInformation() != null
            && p.getAdditionalInformation().contains("Montant"))).isTrue();
  }

  @Test
  void toDocument_withTiersAndVol_buildsTiersDeclaration() {
    Tiers tiers = new Tiers();
    tiers.setNom("T");
    tiers.setPrenom("N");
    tiers.setDateNaissance("1985-05-05");
    tiers.setGenre(new RipolCode("2", "F"));
    tiers.setNationalite(new RipolCode("8100", "CH"));
    tiers.setAdresse(new Adresse("Rue tiers 1", "", "1200", "Genève", "1200", "CH", "8100"));
    tiers.setEmail("t@t.ch");
    tiers.setTelephone("41791111111");
    tiers.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    tiers.setNumeroDocumentIdentite("ZZ");

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.TIERS);
    ip.setTypeRepresentation("Représentation légale");
    ip.setTiers(tiers);

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-08-01");
    vol.setDateFinEvent("2025-08-01");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("TIERS-VOL", ip, Incident.of(vol)));

    assertThat(doc.getPersons()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(doc.getRelations().getPersonLinks()).isNotEmpty();
  }

  @Test
  void toDocument_withOrganisationAndVol_buildsEntrepriseDeclaration() {
    Organisation org = new Organisation();
    org.setNom("SA Demo");
    org.setEmail("info@demo.ch");
    org.setTelephone("0223334455");
    org.setAdresse(new Adresse("Av. Entreprise 10", "", "1200", "Genève", "1200", "CH", "8100"));

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    ip.setPostePersonneMorale("Gérant");
    ip.setOrganisation(org);

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-09-01");
    vol.setDateFinEvent("2025-09-01");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("ORG-VOL", ip, Incident.of(vol)));

    assertThat(doc.getBusinessCases().getFirst().getKey()).isEqualTo(Ech051Constants.BUSINESS_CASE_KEY_ENTREPRISE);
    assertThat(doc.getRelations().getPersonLinks()).hasSize(2);
  }

  @Test
  void toDocument_withoutIncident_stillBuildsPayload() {
    PrePlainte p = new PrePlainte("NO-EVT", basePersonne(), null);
    Ech0051DocumentPayload doc = mapper.toDocument(p);
    assertThat(doc.getEvents()).isEmpty();
    assertThat(doc.getProcessData()).isNotNull();
  }

  @Test
  void toDocument_withoutInformationsPersonnelles_defaultsToIndividual() {
    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-10-01");
    vol.setDateFinEvent("2025-10-01");
    PrePlainte p = new PrePlainte("NULL-IP", null, Incident.of(vol));
    Ech0051DocumentPayload doc = mapper.toDocument(p);
    assertThat(doc).isNotNull();
    assertThat(doc.getBusinessCases().getFirst().getKey()).isEqualTo(Ech051Constants.BUSINESS_CASE_KEY);
  }
}
