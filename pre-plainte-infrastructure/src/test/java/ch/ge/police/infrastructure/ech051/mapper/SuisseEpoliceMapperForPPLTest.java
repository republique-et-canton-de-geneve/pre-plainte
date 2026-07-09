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
import ch.ge.police.core.domain.model.informationspersonnelles.common.TitreSejour;
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
    ip.setLieuOrigine(new RipolCode("6621", "Genève"));
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
    assertThat(doc.getBusinessCases().getFirst().getCaseNumber()).isNull();
  }

  @Test
  void toDocument_mapsCyberAchatWithDirectEnterpriseSellerAsNaturalPerson() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Nom de famille du venduer");
    achat.setPrenomVendeur("Prénom du vendeur");
    achat.setAchatViaPlaceMarche(false);
    achat.setNomEntrepriseVendeur("Nom de l'entreprise");
    achat.setSiteWebEntrepriseVendeur("www.site.ch");
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

    Ech0051DocumentPayload.Person seller = doc.getPersons().stream()
        .filter(person -> person != null && person.getType() == PersonType.NATURAL)
        .filter(person -> "Prénom du vendeur".equals(person.getNaturalIdentity().getFirstName()))
        .findFirst()
        .orElseThrow();

    assertThat(seller.getCommunication().getUri()).isEqualTo("www.site.ch");
    assertThat(seller.getCommunication().getUriProvider()).isEqualTo(Ech051Constants.URI_PROVIDER_ENTREPRISE_VENDEUR);
    assertThat(doc.getPersons().stream().filter(
        person -> person != null && person.getType() == PersonType.LEGAL))
        .extracting(person -> person.getLegalIdentity().getCurrentName())
        .containsExactly(Ech051Constants.INSURER_NAME_NONE);
  }

  @Test
  void toDocument_mapsCyberCommandeFrauduleuse_alignsWithSepModel() {
    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setPrestataire("Boutique en ligne");
    cf.setDateDecouverte("2025-03-01");
    cf.setMontant(250.0);
    cf.setAssurance(true);
    cf.setPrenomContrevenant("Jean");
    cf.setNomContrevenant("Dupont");
    cf.setEmailCommande("fraud@x.ch");
    cf.setTelephoneCommande("41791234567");
    cf.setSiteWebContrevenant("www.shop.ch");
    cf.setLivraisonAdresseLesee(false);
    cf.setAdresseLivraison(new Adresse("Rue étrangère 1", "", "75001", "Paris", "", "FR", null));
    cf.setAdresseContrevenant(new Adresse("Route des Fayards", "", "1200", "Genève", "120000", "CH", "8100"));
    cf.setMoyenPaiementNumeriqueDebite(false);

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");
    cyber.setDescriptionCybercrime("Commande frauduleuse");

    PrePlainte p = new PrePlainte("CYB-CF", basePersonne(), Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    assertThat(doc.getEvents()).hasSize(2);
    assertThat(doc.getEvents().getFirst().getFacts()).isNull();
    assertThat(doc.getEvents().getFirst().getAdditionalInformation()).isEqualTo("Commande frauduleuse");
    assertThat(doc.getEvents().get(1).getActionPlace()).isNull();

    assertThat(doc.getObjects().stream().map(Ech0051DocumentPayload.ObjectItem::getKey))
        .contains(Ech051Constants.OBJECT_KEY_CYBER_FRAUDULENT_ORDER_AMOUNT);

    Ech0051DocumentPayload.Person victim = doc.getPersons().stream()
        .filter(person -> Ech051Constants.PERSON_KEY_TIERS.equals(person.getKey()))
        .findFirst()
        .orElseThrow();
    assertThat(victim.getDeliveredAbroad()).isTrue();
    assertThat(victim.getCreditCardUsed()).isFalse();

    Ech0051DocumentPayload.Person counterparty = doc.getPersons().stream()
        .filter(person -> person.getNaturalIdentity() != null
            && Ech051Constants.IDENTITY_CATEGORY_UNKNOWN.equals(person.getNaturalIdentity().getIdentityCategory()))
        .findFirst()
        .orElseThrow();
    assertThat(counterparty.getAdditionalInformation()).isEqualTo("Boutique en ligne");
    assertThat(counterparty.getNaturalIdentity().getOfficialName()).isEqualTo("Dupont");
    assertThat(counterparty.getAddresses()).hasSize(2);
    assertThat(counterparty.getAddresses().getFirst().getAddressLine()).contains("Paris");

    assertThat(doc.getRelations().getFinancialTransactions()).isEmpty();
    assertThat(doc.getRelations().getEventObjectLinks()).isNotEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isNotEmpty();
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

    assertThat(doc.getObjects()).hasSize(1);
    assertThat(doc.getObjects().getFirst().getKey()).isEqualTo(Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY);
    assertThat(doc.getRelations().getEventObjectLinks()).isEmpty();
    assertThat(doc.getRelations().getObjectPersonLinks()).isEmpty();
    assertThat(doc.getEvents().getFirst().getFacts()).isNull();
    assertThat(doc.getEvents().getFirst().getAdditionalInformation())
        .startsWith("Autre indications;")
        .contains("Arnaque au loyer")
        .contains("URL complète:");
    assertThat(doc.getPersons().stream().filter(x -> x != null && x.getType() == PersonType.NATURAL).count()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void toDocument_mapsCyberAchat_naturalSellerViaMarketplace_withStructuredFields() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Vendeur");
    achat.setPrenomVendeur("Victor");
    achat.setAchatViaPlaceMarche(true);
    achat.setEmailVendeur("v@v.ch");
    achat.setTelephoneVendeur("41790000001");
    achat.setAdresseVendeur(new Adresse("Rue du Marché 2", "", "1200", "Genève", "1200", "CH", "8100"));
    achat.setPlateformeUtilisee(PlateformeUtilisee.RICARDO);
    achat.setMoyenPaiement(MoyenPaiement.PAYPAL);
    achat.setComptePaypalBeneficiaire("buyer@paypal.test");
    achat.setNumeroTransactionPaypal("TX-12345");
    achat.setDateOperation("2025-05-10");
    achat.setMontantDelitAchatLigne("44343,00");
    achat.setArticleNonLivreDescription("Article non livré");
    achat.setPlateformeId("ricardo-99");
    achat.setPrenomBeneficiaire("Jean");
    achat.setNomBeneficiaire("Dupont");
    achat.setSocieteBeneficiaire("ACME");
    achat.setPreuvePaiementIndisponible(true);
    achat.setRaisonAbsencePreuvePaiement("Raison absence preuve");
    achat.setCopieIdentiteTransmiseAuteur(false);
    achat.setCopieIdentiteAuteurTransmise(false);

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

    assertThat(doc.getEvents().getFirst().getFacts()).isNull();
    assertThat(doc.getEvents().getFirst().getAdditionalInformation()).isEqualTo("Colis vide");
    assertThat(doc.getEvents()).hasSize(2);
    assertThat(doc.getEvents().get(1).getKey()).isEqualTo(Ech051Constants.EVENT_KEY_PAYMENT);
    assertThat(doc.getEvents().get(1).getFacts()).isNull();
    assertThat(doc.getEvents().get(1).getAdditionalInformation()).isEqualTo("Colis vide");
    assertThat(doc.getEvents().get(1).getActionPlace()).isNull();

    assertThat(doc.getObjects()).hasSize(1);
    assertThat(doc.getObjects().getFirst().getKey()).isEqualTo(Ech051Constants.OBJECT_KEY_CYBER_UNDELIVERED_ITEM);
    assertThat(doc.getObjects().getFirst().getDescription()).isEqualTo("Article non livré");
    assertThat(doc.getObjects().getFirst().getRealValue()).isEqualTo("44343");

    Ech0051DocumentPayload.Person victim = doc.getPersons().stream()
        .filter(person -> Ech051Constants.PERSON_KEY_TIERS.equals(person.getKey()))
        .findFirst()
        .orElseThrow();
    assertThat(victim.getOnlineShop()).isTrue();
    assertThat(victim.getNoPaymentProofReason()).isEqualTo("Raison absence preuve");
    assertThat(victim.getReporterIdCopySent()).isFalse();
    assertThat(victim.getPerpetratorIdCopyReceived()).isFalse();

    Ech0051DocumentPayload.Person seller = doc.getPersons().stream()
        .filter(person -> person.getNaturalIdentity() != null
            && "Victor".equals(person.getNaturalIdentity().getFirstName()))
        .findFirst()
        .orElseThrow();
    assertThat(seller.getCommunication().getUriProvider()).isEqualTo("Ricardo");
    assertThat(seller.getCommunication().getUri()).isEqualTo("ricardo-99");

    assertThat(doc.getPersons().stream().anyMatch(person ->
        person.getRemark() != null && person.getRemark().contains("ACME"))).isTrue();

    assertThat(doc.getRelations().getEventObjectLinks()).hasSize(1);
    assertThat(doc.getRelations().getObjectPersonLinks()).hasSize(1);
    assertThat(doc.getRelations().getObjectPersonLinks().getFirst().getObjectRef())
        .isEqualTo(Ech051Constants.OBJECT_KEY_CYBER_UNDELIVERED_ITEM);
    assertThat(doc.getRelations().getFinancialTransactions().getFirst().getPersonReceiveRef())
        .isNotEqualTo(seller.getKey());
    assertThat(doc.getRelations().getFinancialTransactions().getFirst().getEventRef())
        .isEqualTo(Ech051Constants.EVENT_KEY_PAYMENT);
  }

  @Test
  void toDocument_mapsCyberAchat_identityCopyAbsenceReasonsOnVictim() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Vendeur");
    achat.setPrenomVendeur("Victor");
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2026-07-01");
    achat.setCopieIdentiteTransmiseAuteur(true);
    achat.setCopieIdentiteTransmiseAuteurDocumentIndisponible(true);
    achat.setRaisonAbsenceCopieIdentiteTransmiseAuteur("Raison absence copie lésé");
    achat.setCopieIdentiteAuteurTransmise(true);
    achat.setCopieIdentiteAuteurDocumentIndisponible(true);
    achat.setRaisonAbsenceCopieIdentiteAuteur("Raison absence copie auteur");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2026-07-01");
    cyber.setDateFinEvent("2026-07-02");

    PrePlainte p = new PrePlainte("ACHAT-ID", basePersonne(), Incident.of(cyber));
    Ech0051DocumentPayload doc = mapper.toDocument(p);

    Ech0051DocumentPayload.Person victim = doc.getPersons().stream()
        .filter(person -> Ech051Constants.PERSON_KEY_TIERS.equals(person.getKey()))
        .findFirst()
        .orElseThrow();

    assertThat(victim.getReporterIdCopySent()).isTrue();
    assertThat(victim.getNoIdCopyPresentReason()).isEqualTo("Raison absence copie lésé");
    assertThat(victim.getPerpetratorIdCopyReceived()).isTrue();
    assertThat(victim.getNoPerpetratorsIdCopyPresentReason()).isEqualTo("Raison absence copie auteur");
  }

  @Test
  void toDocument_mapsCyberAchat_victimIdentityObjectAndLink() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Vendeur");
    achat.setPrenomVendeur("Victor");
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2026-07-01");
    achat.setMontantDelitAchatLigne("2323");
    achat.setArticleNonLivreDescription("Article non livré");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2026-07-01");
    cyber.setDateFinEvent("2026-07-02");

    InformationsPersonnelles ip = basePersonne();
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.CARTE_IDENTITE);
    ip.setNumeroDocumentIdentite("X9876543");
    ip.setTitreSejour(TitreSejour.PERMIS_B);

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("ACHAT-ID-OBJ", ip, Incident.of(cyber)));

    assertThat(doc.getObjects()).hasSize(2);
    Ech0051DocumentPayload.ObjectItem identity = doc.getObjects().stream()
        .filter(object -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(object.getKey()))
        .findFirst()
        .orElseThrow();
    assertThat(identity.getTypeOfObject().getCode()).isEqualTo(Ech051Constants.OBJECT_TYPE_CYBER_IDENTITY_CODE);
    assertThat(identity.getIdentification().getType()).isEqualTo("IDPass");
    assertThat(identity.getIdentification().getNumber()).isEqualTo("X9876543");
    assertThat(identity.getOfficialDocument().getPermitCategory().getCode()).isEqualTo("02");

    assertThat(doc.getRelations().getObjectPersonLinks()).hasSize(2);
    assertThat(doc.getRelations().getObjectPersonLinks().stream()
        .anyMatch(link -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && Ech051Constants.PERSON_KEY_TIERS.equals(link.getPersonRef())
            && link.getPersonRole() == null)).isTrue();
  }

  @Test
  void toDocument_mapsCyberAchatPaypalTransactionNumberSeparatelyFromBeneficiaryEmail() {
    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Nom de famille du venduer");
    achat.setPrenomVendeur("Prénom du vendeur");
    achat.setAchatViaPlaceMarche(false);
    achat.setNomEntrepriseVendeur("Nom de l'entreprise");
    achat.setSiteWebEntrepriseVendeur("www.site.ch");
    achat.setMoyenPaiement(MoyenPaiement.PAYPAL);
    achat.setComptePaypalBeneficiaire("adresse-email@vers-laquelle-argenta-ete.vire.ch");
    achat.setNumeroTransactionPaypal("074343243");
    achat.setDateOperation("2026-07-01");
    achat.setMontantDelitAchatLigne("2132");
    achat.setArticleNonLivreDescription("Artocée non livré");
    achat.setNomBeneficiaire("Nom de famille du bénéficiaire du paiment");
    achat.setPrenomBeneficiaire("Prénom du bénéficiaire du paiement");
    achat.setSocieteBeneficiaire("Société du bénéficiaire du paiement");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2026-07-01");
    cyber.setDateFinEvent("2026-07-02");

    Ech0051DocumentPayload doc = mapper.toDocument(
        new PrePlainte("ACHAT-PAYPAL", basePersonne(), Incident.of(cyber)));

    Ech0051DocumentPayload.FinancialTransaction payment = doc.getRelations().getFinancialTransactions().getFirst();
    assertThat(payment.getPaymentType()).isEqualTo("PAYPAL");
    assertThat(payment.getAccountReceive()).isEqualTo("adresse-email@vers-laquelle-argenta-ete.vire.ch");
    assertThat(payment.getTransactionNumber()).isEqualTo("074343243");
    assertThat(payment.getEventRef()).isEqualTo(Ech051Constants.EVENT_KEY_PAYMENT);

    Ech0051DocumentPayload.Person seller = doc.getPersons().stream()
        .filter(person -> person != null && person.getType() == PersonType.NATURAL)
        .filter(person -> "Prénom du vendeur".equals(person.getNaturalIdentity().getFirstName()))
        .findFirst()
        .orElseThrow();
    assertThat(seller.getCommunication().getUri()).isEqualTo("www.site.ch");
    assertThat(seller.getCommunication().getUriProvider()).isEqualTo(Ech051Constants.URI_PROVIDER_ENTREPRISE_VENDEUR);
  }

  @Test
  void toDocument_mapsCyberAutre_skipsObjectsAndCounterparty() {
    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.AUTRE);
    cyber.setDateDebutEvent("2025-06-01");
    cyber.setDateFinEvent("2025-06-02");
    cyber.setDescriptionCybercrime("Autre infraction");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("CYB-AUTRE", basePersonne(), Incident.of(cyber)));

    assertThat(doc.getObjects()).hasSize(1);
    assertThat(doc.getObjects().getFirst().getKey()).isEqualTo(Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY);
  }

  @Test
  void toDocument_commandeFrauduleuse_mapsCounterpartyPrestataire() {
    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setPrestataire("Ma boutique");
    cf.setDateDecouverte("2025-07-01");
    cf.setMontant(99.5);
    cf.setAssurance(false);
    cf.setPrenomContrevenant("Paul");
    cf.setNomContrevenant("Martin");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-07-01");
    cyber.setDateFinEvent("2025-07-01");
    cyber.setDescriptionCybercrime("Usurpation");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("CMD-INFOS", basePersonne(), Incident.of(cyber)));

    assertThat(doc.getPersons().stream().anyMatch(
        p -> p != null
            && "Ma boutique".equals(p.getAdditionalInformation())
            && p.getNaturalIdentity() != null
            && "Martin".equals(p.getNaturalIdentity().getOfficialName()))).isTrue();
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
  void toDocument_withOrganisationAndCybercrime_buildsRepresentativeRelations() {
    Organisation org = new Organisation();
    org.setNom("SA Demo");
    org.setEmail("info@demo.ch");
    org.setTelephone("0223334455");
    org.setAdresse(new Adresse("Av. Entreprise 10", "", "1200", "Genève", "1200", "CH", "8100"));

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    ip.setPostePersonneMorale("Gérant");
    ip.setOrganisation(org);

    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Vendeur");
    achat.setPrenomVendeur("Jean");
    achat.setMoyenPaiement(MoyenPaiement.IBAN);
    achat.setIbanBeneficiaire("CH9300762011623852957");
    achat.setDateOperation("2025-09-01");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDateDebutEvent("2025-09-01");
    cyber.setDateFinEvent("2025-09-01");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("ORG-CYB", ip, Incident.of(cyber)));

    assertThat(doc.getRelations().getPersonLinks()).anyMatch(link ->
        Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(link.getPerson1Ref())
            && Ech051Constants.PERSON_KEY_ORGANISATION.equals(link.getPerson2Ref()));
    assertThat(doc.getRelations().getInvolvedParties()).anyMatch(party ->
        Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(party.getPersonRef()));
    assertThat(doc.getRelations().getInvolvedParties()).anyMatch(party ->
        Ech051Constants.PERSON_KEY_INFORMANT.equals(party.getPersonRef()));
  }

  @Test
  void toDocument_withOrganisationAndCybercrime_linksIdentityObjectToDeclarant() {
    Organisation org = new Organisation();
    org.setNom("Demo SA");
    org.setEmail("contact@demo-sa.example.org");
    org.setTelephone("4122334455");
    org.setAdresse(new Adresse("Rue du Test 12", "", "1200", "Genève", "1200", "CH", "8100"));

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    ip.setPostePersonneMorale("Gérant");
    ip.setOrganisation(org);
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.CARTE_IDENTITE);
    ip.setNumeroDocumentIdentite("X9876543");
    ip.setTitreSejour(TitreSejour.PERMIS_B);

    AchatNonRecu achat = new AchatNonRecu();
    achat.setNomVendeur("Nom de famille du vendeur");
    achat.setPrenomVendeur("Prénom du vendeur");
    achat.setMoyenPaiement(MoyenPaiement.AUTRE);
    achat.setMoyenPaiementAutre("Autre méthode de paiment");
    achat.setDateOperation("2026-07-08");
    achat.setMontantDelitAchatLigne("2342");
    achat.setArticleNonLivreDescription("Article non livré");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(achat);
    cyber.setDatePremierContact("2026-07-08");
    cyber.setDateDernierContact("2026-07-09");
    cyber.setDescriptionCybercrime("Veuillez décrire le déroulement des faits");

    Ech0051DocumentPayload doc = mapper.toDocument(new PrePlainte("ORG-CYB-ID", ip, Incident.of(cyber)));

    assertThat(doc.getObjects().stream()
        .anyMatch(object -> Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(object.getKey()))).isTrue();
    assertThat(doc.getRelations().getObjectPersonLinks()).anyMatch(link ->
        Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && Ech051Constants.PERSON_KEY_DECLARANT_ENTREPRISE.equals(link.getPersonRef())
            && link.getPersonRole() == null);
    assertThat(doc.getRelations().getObjectPersonLinks()).noneMatch(link ->
        Ech051Constants.OBJECT_KEY_CYBER_VICTIM_IDENTITY.equals(link.getObjectRef())
            && Ech051Constants.PERSON_KEY_ORGANISATION.equals(link.getPersonRef()));
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
