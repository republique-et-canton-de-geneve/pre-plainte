package ch.ge.police.infrastructure.pdf;

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
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.LienAvecPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import ch.ge.police.core.domain.model.informationspersonnelles.common.TypeDocumentIdentite;
import ch.ge.police.core.domain.model.rendezvous.CreneauRendezVous;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ge.police.infrastructure.pdf.PdfGenerationAdapter.RIPOL_AUTRE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationAdapterTest {

  private PdfGenerationAdapter adapter;

  @BeforeEach
  void setup() {
    adapter = new PdfGenerationAdapter();
  }

  private InformationsPersonnelles basePersonne() {
    InformationsPersonnelles ip = new InformationsPersonnelles();
    ip.setNom("TEST");
    ip.setPrenom("Test");
    ip.setGenre(new RipolCode("F", "Femme"));
    ip.setNationalite(new RipolCode("FR", "France"));
    ip.setDateNaissance("1990-01-01");
    ip.setAdresse(new Adresse("Rue Test", "1", "1200", "Genève", "1200", "Suisse", "8212"));
    ip.setTelephone("4178123456");
    ip.setEmail("test@test.ch");
    ip.setLienAvecPersonne(LienAvecPersonne.MOI_MEME);
    ip.setTypeDocumentIdentite(TypeDocumentIdentite.PASSEPORT);
    ip.setNumeroDocumentIdentite("ABC123");
    return ip;
  }

  private ObjetIncident objetComplet() {
    ObjetIncident o = new ObjetIncident();

    o.setType(new RipolCode("TEL", "Téléphone"));
    o.setFabricant(new RipolCode("APPLE", "Apple"));
    o.setModele(new RipolCode("IPHONE", "iPhone"));
    o.setCouleur(new RipolCode("NOIR", "Noir"));
    o.setCouleurSecondaire(new RipolCode("GRIS", "Gris"));
    o.setNumeroCadre("CADRE123");
    o.setVin("VIN123");
    o.setNumeroSerie("SERIE123");
    o.setNumeroIMEI("IMEI123");
    o.setPurchaseDate("2024-01-01");
    o.setRealValue("800");

    return o;
  }

  private Adresse adresse(
    String adresse,
    String npa,
    String localite,
    String pays,
    String paysCode
  ) {
    return new Adresse(adresse, null, npa, localite, null, pays, paysCode);
  }

  private void assertPdf(byte[] pdf) {
    assertNotNull(pdf);
    assertTrue(pdf.length > 500);
  }

  private String extractPdfText(byte[] pdf) throws Exception {
    try (PdfReader reader = new PdfReader(pdf)) {
      PdfTextExtractor extractor = new PdfTextExtractor(reader);
      return IntStream.rangeClosed(1, reader.getNumberOfPages())
        .mapToObj(page -> {
          try {
            return extractor.getTextFromPage(page);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        })
        .collect(Collectors.joining("\n"));
    }
  }

  private void assertPdfTextContains(String text, String expected) {
    assertTrue(text.replaceAll("\\s+", "").contains(expected.replaceAll("\\s+", "")));
  }

  @Test
  void shouldGeneratePdfVolComplete() {

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01T10:00");
    vol.setDateFinEvent("2025-01-01T11:00");
    vol.setVolDansVehicule(true);
    vol.setObjetsVoles(List.of(objetComplet()));
    vol.setAvezVousDegradation(true);

    PrePlainte p = new PrePlainte("VOL-1", basePersonne(), Incident.of(vol));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldRenderEventHoursInPdf() throws Exception {
    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01T10:00");
    vol.setDateFinEvent("2025-01-01T11:30");
    vol.setVolDansVehicule(false);

    PrePlainte p = new PrePlainte("VOL-HOURS", basePersonne(), Incident.of(vol));

    String text = extractPdfText(adapter.generatePdf(p));

    assertPdfTextContains(text, "Date de début de l'événement");
    assertPdfTextContains(text, "01.01.2025 à 10:00");
    assertPdfTextContains(text, "Date de fin de l'événement");
    assertPdfTextContains(text, "01.01.2025 à 11:30");
  }

  @Test
  void shouldGeneratePdfVolWithJustificationAbsenceIMEI() {

    ObjetIncident o = objetComplet();
    o.setJustificationAbsenceIMEI("Appareil acheté d'occasion sans boîte");

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-02");
    vol.setDateFinEvent("2025-01-02");
    vol.setObjetsVoles(List.of(o));

    PrePlainte p = new PrePlainte("VOL-IMEI", basePersonne(), Incident.of(vol));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfVolWithoutObject() {

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01");
    vol.setDateFinEvent("2025-01-01");
    vol.setVolDansVehicule(false);

    PrePlainte p = new PrePlainte("VOL-2", basePersonne(), Incident.of(vol));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfDommageComplete() {

    DommageMateriel d = new DommageMateriel();

    d.setDateDebutEvent("2025-02-01");
    d.setDateFinEvent("2025-02-01");
    d.setTypeDommage(TypeDommage.DOMMAGE_VEHICULE);
    d.setObjetDegrades(List.of(objetComplet()));
    d.setMontantEstime(900.0);
    d.setDevise("CHF");
    d.setDescription("Rayure");
    d.setConstatPresent(true);
    d.setDateConstat("2025-02-02");
    d.setNaturesDommage(List.of(NatureDommage.DEGRADATIONS));

    PrePlainte p = new PrePlainte("DOM-1", basePersonne(), Incident.of(d));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfDommageWithoutNature() {

    DommageMateriel d = new DommageMateriel();

    d.setDateDebutEvent("2025-02-01");
    d.setDateFinEvent("2025-02-01");
    d.setConstatPresent(false);

    PrePlainte p = new PrePlainte("DOM-2", basePersonne(), Incident.of(d));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfCyberCommandeFrauduleuse() {

    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setPrestataire("shop");
    cf.setDateDecouverte("2025-03-01");
    cf.setMontant(300.0);
    cf.setAssurance(true);
    cf.setEmailCommande("john@doe.ch");
    cf.setTelephoneCommande("4178123456");
    cf.setLivraisonAdresseLesee(true);

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");
    cyber.setDescriptionCybercrime("Fraude commande");

    PrePlainte p = new PrePlainte("CYB-1", basePersonne(), Incident.of(cyber));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfCyberCommandeFrauduleuseMinimalFields() {

    CommandeFrauduleuse cf = new CommandeFrauduleuse();
    cf.setDateDecouverte("2025-04-01");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.COMMANDE_FRAUDULEUSE);
    cyber.setCommandeFrauduleuse(cf);
    cyber.setDateDebutEvent("2025-04-01");
    cyber.setDateFinEvent("2025-04-01");
    cyber.setDescriptionCybercrime("Desc min");

    PrePlainte p = new PrePlainte("CYB-CF-MIN", basePersonne(), Incident.of(cyber));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfCyberAchatNonRecu() {

    AchatNonRecu a = new AchatNonRecu();

    a.setNomVendeur("Doe");
    a.setPrenomVendeur("John");
    a.setEmailVendeur("john@doe.ch");
    a.setTelephoneVendeur("4178123456");
    a.setAdresseVendeur(new Adresse("Rue test", "", "1200", "Genève", null, "Suisse", "8100"));

    a.setPlateformeUtilisee(PlateformeUtilisee.ANIBIS);
    a.setMoyenPaiement(MoyenPaiement.TWINT);
    a.setDateOperation("2025-03-01");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(a);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");

    PrePlainte p = new PrePlainte("CYB-2", basePersonne(), Incident.of(cyber));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfCyberAchatNonRecuWithOptionalFlagsAndPaymentDetails() {

    AchatNonRecu a = new AchatNonRecu();
    a.setNomVendeur("Seller");
    a.setPrenomVendeur("Sam");
    a.setEmailVendeur("s@x.ch");
    a.setEmailVendeurInconnu(false);
    a.setTelephoneVendeurInconnu(true);
    a.setAdresseVendeurInconnue(false);
    a.setAdresseVendeur(new Adresse("Av. des Champs", "BP 9", "1200", "Genève", "1200", "CH", "8100"));
    a.setAchatViaPlaceMarche(true);
    a.setPlateformeUtilisee(PlateformeUtilisee.AUTRE);
    a.setPlateformeAutre("Marché perso");
    a.setPlateformeId("https://listing.example/id/99");
    a.setNomEntrepriseVendeur("Shop sarl");
    a.setSiteWebEntrepriseVendeur("https://shop.example");
    a.setAnnonceDocumentIndisponible(true);
    a.setRaisonAbsenceAnnonce("Fichier corrompu");
    a.setMoyenPaiement(MoyenPaiement.IBAN);
    a.setIbanBeneficiaire("CH9300762011623852957");
    a.setComptePaypalBeneficiaire("pay@pal.ch");
    a.setNumeroTwintBeneficiaire("+41790000000");
    a.setAdresseWalletCrypto("0xabc");
    a.setHashTransactionCrypto("0xdeadbeef");
    a.setSocieteBeneficiaire("HoldCo");
    a.setNomBeneficiaire("Ben");
    a.setPrenomBeneficiaire("Ef");
    a.setMontantDelitAchatLigne("199.50");
    a.setArticleNonLivreDescription("Console");
    a.setDateOperation("2025-03-15T12:00:00+01:00");
    a.setPreuvePaiementIndisponible(true);
    a.setRaisonAbsencePreuvePaiement("Banque injoignable");
    a.setCopieIdentiteTransmiseAuteur(true);
    a.setCopieIdentiteAuteurTransmise(false);

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.ACHAT_NON_RECU);
    cyber.setAchatNonRecu(a);
    cyber.setDatePremierContact("2025-02-01");
    cyber.setDateDernierContact("2025-02-10");
    cyber.setDescriptionCybercrime("Colis jamais expédié");

    PrePlainte p = new PrePlainte("CYB-ACHAT-RICHE", basePersonne(), Incident.of(cyber));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfCyberFausseAnnonce() {

    FausseAnnonce f = new FausseAnnonce();
    f.setUrlComplete("https://anibis.example/listing");
    f.setTitreAnnonce("Appartement Genève");
    f.setNomBailleur("Dupont");
    f.setEmailBailleur("dupont@test.ch");
    f.setEmailBailleurInconnu(false);
    f.setTelephoneBailleur("4178123456");
    f.setTelephoneBailleurInconnu(false);
    f.setAdresseBienImmobilier("Rue Genève");
    f.setMontantDemande(1500d);
    f.setModePaiementDemande("Cash");

    Cybercrime cyber = new Cybercrime();
    cyber.setTypeCybercrime(TypeCybercrime.FAUSSE_ANNONCE);
    cyber.setFausseAnnonce(f);
    cyber.setDateDebutEvent("2025-03-01");
    cyber.setDateFinEvent("2025-03-01");

    PrePlainte p = new PrePlainte("CYB-3", basePersonne(), Incident.of(cyber));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfWithTiers() {

    Tiers t = new Tiers();
    t.setNom("Dupont");
    t.setPrenom("Paul");
    t.setDateNaissance("1980-01-01");
    t.setAdresse(new Adresse("Rue tiers", "1", "1200", "Genève", "1200", "Suisse", "8212"));

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.TIERS);
    ip.setTiers(t);

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-04-01");
    vol.setDateFinEvent("2025-04-01");

    PrePlainte p = new PrePlainte("TIERS-1", ip, Incident.of(vol));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfWithOrganisation() {

    Organisation org = new Organisation();
    org.setNom("ACME");
    org.setEmail("contact@acme.ch");
    org.setTelephone("0221234567");
    org.setAdresse(new Adresse("Rue entreprise", "1", "1200", "Genève", "1200", "Suisse", "8212"));

    InformationsPersonnelles ip = basePersonne();
    ip.setLienAvecPersonne(LienAvecPersonne.ENTREPRISE);
    ip.setOrganisation(org);

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-05-01");
    vol.setDateFinEvent("2025-05-01");

    PrePlainte p = new PrePlainte("ORG-1", ip, Incident.of(vol));

    assertPdf(adapter.generatePdf(p));
  }

  @Test
  void shouldGeneratePdfVolWithRipolAutre() {

    ObjetIncident objet = new ObjetIncident();

    objet.setType(new RipolCode("TEL", "Téléphone"));

    objet.setFabricant(new RipolCode(RIPOL_AUTRE, RIPOL_AUTRE));
    objet.setFabricantAutre("FabricantCustom");

    objet.setModele(new RipolCode(RIPOL_AUTRE, RIPOL_AUTRE));
    objet.setModeleAutre("ModeleCustom");

    objet.setNumeroCadreInconnu(true);
    objet.setVinInconnu(true);
    objet.setPlaqueInconnu(true);
    objet.setNumeroSerieInconnu(true);
    objet.setNumeroIMEIInconnu(true);

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01");
    vol.setDateFinEvent("2025-01-01");
    vol.setObjetsVoles(List.of(objet));

    PrePlainte prePlainte = new PrePlainte("VOL-RIPOL", basePersonne(), Incident.of(vol));

    byte[] pdf = adapter.generatePdf(prePlainte);

    assertPdf(pdf);
  }

  @Test
  void shouldRenderStolenPlateNumberInPdfRows() throws Exception {
    ObjetIncident plaque = new ObjetIncident();
    plaque.setCategorieObjet("plaque");
    plaque.setPlaqueNumero("GE123456");

    Vol vol = new Vol();
    vol.setVolDansVehicule(false);
    vol.setObjetsVoles(List.of(plaque));
    vol.setAvezVousDegradation(false);

    List<String[]> rows = new ArrayList<>();
    var handleVol = PdfGenerationAdapter.class.getDeclaredMethod("handleVol", List.class, Vol.class);
    handleVol.setAccessible(true);
    handleVol.invoke(adapter, rows, vol);

    assertTrue(rows.stream().anyMatch(row ->
        "Numéro de plaque".equals(row[0]) && "GE123456".equals(row[1])));
  }

  @Test
  void shouldGeneratePdfWithRendezVous() {

    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01");
    vol.setDateFinEvent("2025-01-01");

    CreneauRendezVous creneau = new CreneauRendezVous("id-rdv", "2025-06-01", "10:00", "10:30", "Hôtel de police", "PPEL");

    PrePlainte prePlainte = new PrePlainte("RDV-001", basePersonne(), Incident.of(vol));

    prePlainte.setCreneauRendezVous(creneau);

    byte[] pdf = adapter.generatePdf(prePlainte);

    assertPdf(pdf);
  }

  @Test
  void shouldHandleInvalidDateFormat() {

    Vol vol = new Vol();

    vol.setDateDebutEvent("DATE_INVALIDE");
    vol.setDateFinEvent("DATE_INVALIDE");

    PrePlainte prePlainte = new PrePlainte("DATE-ERR", basePersonne(), Incident.of(vol));

    byte[] pdf = adapter.generatePdf(prePlainte);

    assertPdf(pdf);
  }

  @Test
  void shouldGeneratePdfWithSecondaryEventAddress() {
    Vol vol = new Vol();
    vol.setDateDebutEvent("2025-01-01T10:00");
    vol.setDateFinEvent("2025-01-01T11:00");
    vol.setIsTrajet(true);
    vol.setAdresseIncident(new Adresse("SBB Bahnhof", null, "2494", "Basel", "405100", "Suisse", "8212"));
    vol.setAdresseIncidentSecondaire(new Adresse("Gare Cornavin", null, "1201", "Genève", "120000", "Suisse", "8212"));

    PrePlainte p = new PrePlainte("VOL-TRAJET-1", basePersonne(), Incident.of(vol));

    byte[] pdf = adapter.generatePdf(p);

    assertPdf(pdf);
  }

  @Test
  void shouldRenderVolAddressLabelsWithoutTrajetSuffixWhenNotTrajet() throws Exception {
    Vol vol = new Vol();
    vol.setAdresseIncident(new Adresse("Rue du Vol 1", null, "1201", "Genève", "120000", "Suisse", "8212"));
    vol.setIsTrajet(false);

    List<String[]> rows = new ArrayList<>();
    var handleVol = PdfGenerationAdapter.class.getDeclaredMethod("handleVol", List.class, Vol.class);
    handleVol.setAccessible(true);
    handleVol.invoke(adapter, rows, vol);

    assertTrue(rows.stream().anyMatch(row -> "Adresse du vol".equals(row[0]) && "Rue du Vol 1".equals(row[1])));
    assertTrue(rows.stream().anyMatch(row -> "Localité du vol".equals(row[0]) && "Genève".equals(row[1])));
    assertFalse(rows.stream().anyMatch(row -> "Adresse de départ".equals(row[0])));
  }

  @Test
  void shouldKeepDepartureAndArrivalLabelsWhenVolIsTrajet() throws Exception {
    Vol vol = new Vol();
    vol.setAdresseIncident(new Adresse("SBB Bahnhof", null, "2494", "Basel", "405100", "Suisse", "8212"));
    vol.setAdresseIncidentSecondaire(new Adresse("Gare Cornavin", null, "1201", "Genève", "120000", "Suisse", "8212"));
    vol.setIsTrajet(true);

    List<String[]> rows = new ArrayList<>();
    var handleVol = PdfGenerationAdapter.class.getDeclaredMethod("handleVol", List.class, Vol.class);
    handleVol.setAccessible(true);
    handleVol.invoke(adapter, rows, vol);

    assertTrue(rows.stream().anyMatch(row -> "Adresse de départ".equals(row[0]) && "SBB Bahnhof".equals(row[1])));
    assertTrue(rows.stream().anyMatch(row -> "Adresse d'arrivée".equals(row[0]) && "Gare Cornavin".equals(row[1])));
  }

  @Test
  void shouldReturnFalseWhenAllFieldsAreNull() {
    Adresse a = adresse(null, null, null, null, null);
    assertFalse(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnFalseWhenAllFieldsAreBlank() {
    Adresse a = adresse(" ", " ", " ", " ", " ");
    assertFalse(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnTrueWhenAdresseIsFilled() {
    Adresse a = adresse("Rue Test", null, null, null, null);
    assertTrue(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnTrueWhenNpaIsFilled() {
    Adresse a = adresse(null, "1200", null, null, null);
    assertTrue(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnTrueWhenLocaliteIsFilled() {
    Adresse a = adresse(null, null, "Genève", null, null);
    assertTrue(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnTrueWhenPaysIsFilled() {
    Adresse a = adresse(null, null, null, "Suisse", null);
    assertTrue(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldReturnTrueWhenPaysCodeIsFilled() {
    Adresse a = adresse(null, null, null, null, "CH");
    assertTrue(PdfGenerationAdapter.adresseHasAnyField(a));
  }

  @Test
  void shouldGenerateFilePdf() {
    PrePlainte p = new PrePlainte("TEST-1", basePersonne(), Incident.of(new Vol()));

    Fichier fichier = adapter.generateFilePdf(p);

    assertNotNull(fichier);
    assertTrue(fichier.nom().startsWith("TEST-1_"));
    assertTrue(fichier.nom().endsWith(".pdf"));
    assertEquals("application/pdf", fichier.typeMime());
    assertNotNull(fichier.contenuBase64());
    assertFalse(fichier.contenuBase64().isBlank());
  }

  @Test
  void shouldContainValidPdfContent() {
    PrePlainte p = new PrePlainte("TEST-2", basePersonne(), Incident.of(new Vol()));

    Fichier fichier = adapter.generateFilePdf(p);

    byte[] decoded = Base64.getDecoder().decode(fichier.contenuBase64());

    assertNotNull(decoded);
    assertTrue(decoded.length > 500);
  }

  @Test
  void shouldThrowExceptionWhenPdfGenerationFails() {
    PdfGenerationAdapter spy = Mockito.spy(new PdfGenerationAdapter());

    PrePlainte p = new PrePlainte("ERR-1", basePersonne(), Incident.of(new Vol()));

    Mockito.doThrow(new RuntimeException("boom"))
      .when(spy).generatePdf(Mockito.any());

    assertThrows(IllegalStateException.class, () -> spy.generateFilePdf(p));
  }

  @Test
  void shouldThrowExceptionWhenValidationFails() {
    PdfGenerationAdapter spy = Mockito.spy(new PdfGenerationAdapter());

    PrePlainte p = new PrePlainte("ERR-2", basePersonne(), Incident.of(new Vol()));

    Mockito.doReturn(new byte[0])
      .when(spy).generatePdf(Mockito.any());

    assertThrows(IllegalStateException.class, () -> spy.generateFilePdf(p));
  }
}
