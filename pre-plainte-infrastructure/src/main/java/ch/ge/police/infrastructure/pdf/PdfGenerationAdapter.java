package ch.ge.police.infrastructure.pdf;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.Adresse;
import ch.ge.police.core.domain.model.common.RipolCode;
import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.common.Incident;
import ch.ge.police.core.domain.model.event.cybercrime.Cybercrime;
import ch.ge.police.core.domain.model.event.cybercrime.common.AchatNonRecu;
import ch.ge.police.core.domain.model.event.cybercrime.common.CommandeFrauduleuse;
import ch.ge.police.core.domain.model.event.cybercrime.common.FausseAnnonce;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.NatureDommage;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.core.domain.model.fichier.Fichier;
import ch.ge.police.core.domain.model.informationspersonnelles.InformationsPersonnelles;
import ch.ge.police.core.domain.model.informationspersonnelles.common.InfosPersonne;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Organisation;
import ch.ge.police.core.domain.model.informationspersonnelles.common.Tiers;
import ch.ge.police.core.domain.util.FichierValidator;
import ch.ge.police.core.port.in.PdfGenerationUseCase;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.GrayColor;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.slf4j.MDC;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PdfGenerationAdapter implements PdfGenerationUseCase {

  private static final String TRACE_ID = "traceId";
  private static final String EVENT_GENERATION_HASH_PDF = "generation_hash_pdf";
  private static final String EVENT_GENERATION_PDF_SUCCESS = "generation_pdf_success";
  private static final String EVENT_GENERATION_PDF_FAILURE = "generation_pdf_failure";

  private static final int SECTION_TABLE_COLUMN_COUNT = 2;
  private static final int TABLE_FULL_WIDTH_PERCENT = 100;
  private static final int DOCUMENT_MARGIN_LEFT = 50;
  private static final int DOCUMENT_MARGIN_RIGHT = 50;
  private static final int DOCUMENT_MARGIN_TOP = 300;
  private static final int DOCUMENT_MARGIN_BOTTOM = 80;
  private static final int MARGIN_LEFT = 50;
  private static final int MARGIN_RIGHT = 50;
  private static final int MARGIN_TOP = 80;
  private static final int MARGIN_BOTTOM = 60;
  private static final int SECTION_SPACING_BEFORE = 15;
  private static final int SECTION_SPACING_AFTER = 8;
  private static final int SECTION_COLUMN_LABEL_WIDTH = 3;
  private static final int SECTION_COLUMN_VALUE_WIDTH = 7;
  private static final int TABLE_SPACING_AFTER = 15;

  private static final int TITLE_SIZE = 16;
  private static final int SUBTITLE_SIZE = 12;
  private static final int NORMAL_SIZE = 10;

  private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, TITLE_SIZE);
  private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, SUBTITLE_SIZE);
  private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, NORMAL_SIZE);
  private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, NORMAL_SIZE);
  private static final Font WARNING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, NORMAL_SIZE);

  public static final float SPACING_BEFORE_WARNING = 40f;
  public static final float SPACING_BEFORE_APPOINTMENT_TEXT = 20f;
  private static final float CELL_BORDER_WIDTH = 0.5f;
  private static final float CELL_PADDING = 6f;
  private static final float BORDER_GREY = 0.78f;
  private static final float WARNING_UNDERLINE_THICKNESS = 0.2f;
  private static final float WARNING_UNDERLINE_YOFFSET = -3f;

  private static final DateTimeFormatter INPUT_DATE =
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final DateTimeFormatter INPUT_DATE_TIME =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private static final DateTimeFormatter DATE_FORMAT =
    DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private static final DateTimeFormatter DATE_TIME_FORMAT =
    DateTimeFormatter.ofPattern("dd.MM.yyyy 'à' HH:mm");

  public static final String RIPOL_AUTRE = "AUTRE";

  @Override
  public byte[] generatePdf(final PrePlainte prePlainte) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      final Document document = new Document(PageSize.A4, DOCUMENT_MARGIN_LEFT, DOCUMENT_MARGIN_RIGHT, DOCUMENT_MARGIN_TOP, DOCUMENT_MARGIN_BOTTOM);
      final PdfWriter writer = PdfWriter.getInstance(document, baos);
      writer.setPageEvent(new HeaderFooter(prePlainte));

      document.open();

      addFirstPage(document, prePlainte);
      addInformationsPersonnelles(document, prePlainte);
      addIncident(document, prePlainte);
      addRendezVous(document, prePlainte);

      document.close();
      return baos.toByteArray();

    } catch (Exception e) {
      throw new PdfGenerationException("Erreur génération PDF", e);
    }
  }

  private void addFirstPage(final Document document, final PrePlainte prePlainte)
    throws DocumentException {

    Paragraph title = new Paragraph(
      "Récépissé de votre pré-plainte " + safe(prePlainte.getDemandeId()),
      TITLE_FONT
    );
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    Paragraph date = new Paragraph(
      "Du " + LocalDate.now().format(DATE_FORMAT),
      TITLE_FONT
    );
    date.setAlignment(Element.ALIGN_CENTER);
    document.add(date);

    document.add(Chunk.NEWLINE);

    Paragraph warning = new Paragraph();
    warning.setSpacingBefore(SPACING_BEFORE_WARNING);

    warning.add(new Chunk("Attention :", WARNING_FONT)
      .setUnderline(WARNING_UNDERLINE_THICKNESS , WARNING_UNDERLINE_YOFFSET));

    warning.add(new Chunk(
      " Ce récépissé est délivré uniquement à titre informatif et ne possède aucune valeur juridique. "
        + "Seul le dépôt de plainte, dûment complété et validé lors du rendez-vous, permettra d’engager la procédure officielle.",
      WARNING_FONT
    ));

    document.add(warning);
  }

  private void addInformationsPersonnelles(final Document document, final PrePlainte prePlainte)
    throws DocumentException {

    final InformationsPersonnelles ip = prePlainte.getInformationsPersonnelles();

    document.setMargins(MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
    document.newPage();

    List<String[]> rows = new ArrayList<>();

    addIfNotNull(rows, "Nom", ip.getNom());
    addIfNotNull(rows, "Nom de naissance", ip.getNomNaissance());
    addIfNotNull(rows, "Prénom", ip.getPrenom());
    addIfNotNull(rows, "Genre", ip.getGenreLabel());
    addIfNotNull(rows, "Nationalité", ip.getNationaliteLabel());
    addIfNotNull(rows, "Titre de séjour",
      ip.getTitreSejour() != null ? ip.getTitreSejour().getLabel() : null);
    addIfNotNull(rows, "Date de naissance", formatDate(ip.getDateNaissance()));

    addAdresse(ip.getAdresse(), rows);
    addDocIdentite(ip, rows);
    addMoyenContact(ip.getEmail(), ip.getTelephone(), rows);

    addSection(document, "INFORMATIONS PERSONNELLES", rows);

    if (ip.hasTiers()) {
      addTiers(document, ip.getTiers());
    } else if (ip.hasOrganisation()) {
      addOrganisation(document, ip.getOrganisation());
    }
  }

  private void addTiers(final Document document, final Tiers tiers)
    throws DocumentException {

    document.newPage();
    List<String[]> rows = new ArrayList<>();

    addIfNotNull(rows, "Nom", tiers.getNom());
    addIfNotNull(rows, "Prénom", tiers.getPrenom());
    addIfNotNull(rows, "Genre", tiers.getGenreLabel());
    addIfNotNull(rows, "Nationalité", tiers.getNationaliteLabel());
    addIfNotNull(rows, "Date de naissance", formatDate(tiers.getDateNaissance()));

    addAdresse(tiers.getAdresse(), rows);
    addDocIdentite(tiers, rows);
    addMoyenContact(tiers.getEmail(), tiers.getTelephone(), rows);

    addSection(document, "INFORMATIONS CONCERNANT LA PERSONNE LÉSÉE", rows);
  }

  private void addOrganisation(final Document document, final Organisation org)
    throws DocumentException {

    document.newPage();
    List<String[]> rows = new ArrayList<>();

    addIfNotNull(rows, "Nom", org.getNom());
    addAdresse(org.getAdresse(), rows);
    addMoyenContact(org.getEmail(), org.getTelephone(), rows);

    addSection(document, "INFORMATIONS SUR L'ORGANISATION", rows);
  }

  private void addDocIdentite(InfosPersonne ip, List<String[]> rows) {
    addIfNotNull(rows, "Type de pièce d'identité",
      ip.getTypeDocumentIdentite() != null ? ip.getTypeDocumentIdentite().getLabel() : null);
    addIfNotNull(rows, "Numéro de pièce d'identité",
      ip.getNumeroDocumentIdentite());
  }

  private void addMoyenContact(String email, String telephone, List<String[]> rows) {
    addIfNotNull(rows, "Courriel", email);
    addIfNotNull(rows, "Téléphone", telephone);
  }

  private void addAdresse(Adresse adresse, List<String[]> rows) {
    if (adresse != null && !adresse.localite().isBlank()) {
      addIfNotNull(rows, "Adresse", adresse.adresse());
      addIfNotNull(rows, "Localité", adresse.localite());
      addIfNotNull(rows, "Code postal", adresse.npa());
      addIfNotNull(rows, "Pays", adresse.pays());
    }
  }

  private void addAdresseEvenement(String suffixe, Adresse adresse, List<String[]> rows) {
    if (adresse == null || !adresseHasAnyField(adresse)) {
      return;
    }

    addIfNotNull(rows, "Adresse " + suffixe, adresse.adresse());
    addIfNotNull(rows, "Localité " + suffixe, adresse.localite());
    addIfNotNull(rows, "Code postal " + suffixe, adresse.npa());
    addIfNotNull(rows, "Pays " + suffixe, adresse.pays());
  }


  static boolean adresseHasAnyField(Adresse a) {
    return (a.adresse() != null && !a.adresse().isBlank())
      || (a.npa() != null && !a.npa().isBlank())
      || (a.localite() != null && !a.localite().isBlank())
      || (a.pays() != null && !a.pays().isBlank())
      || (a.paysCode() != null && !a.paysCode().isBlank());
  }

  private void addIncident(final Document document, final PrePlainte prePlainte)
    throws DocumentException {

    document.newPage();

    final Incident incident = prePlainte.getIncident();
    final IncidentBase details = incident.getDetails();

    List<String[]> rows = new ArrayList<>();
    addIfNotNull(rows, "Type d'incident", incident.getTypeIncident().label());

    switch (incident.getTypeIncident()) {
      case VOL -> handleVol(rows, (Vol) details);
      case DOMMAGE -> handleDommage(rows, (DommageMateriel) details);
      case CYBER -> handleCybercrime(rows, (Cybercrime) details);
    }

    addIfNotNull(rows, "Date de début de l'événement", formatEventDate(details.getDateDebutEvent()));
    addIfNotNull(rows, "Date de fin de l'événement", formatEventDate(details.getDateFinEvent()));

    addSection(document, "EVÈNEMENTS", rows);
  }

  private void addRendezVous(final Document document, final PrePlainte prePlainte)
    throws DocumentException {

    document.newPage();

    if (prePlainte.getCreneauRendezVous() == null) {
      return;
    }

    List<String[]> rows = new ArrayList<>();
    rows.add(new String[]{
      "Date",
      formatDate(prePlainte.getCreneauRendezVous().date())
        + " de " + prePlainte.getCreneauRendezVous().heureDebut()
        + " à " + prePlainte.getCreneauRendezVous().heureFin()
    });
    rows.add(new String[]{
      "Lieu",
      prePlainte.getCreneauRendezVous().lieu()
    });

    addSection(document, "DATE DU RENDEZ-VOUS INITIAL", rows);

    Paragraph end = new Paragraph(
      "Veuillez noter que la date du rendez-vous est celle mentionnée lors de votre dépôt. "
        + "En cas de modification, aucun avenant ne sera envoyé.",
      NORMAL_FONT
    );
    end.setSpacingBefore(SPACING_BEFORE_APPOINTMENT_TEXT);
    document.add(end);
  }

  private void handleVol(List<String[]> rows, Vol v) {
    addBooleanOuiNonPdf(rows, "Les objets ont-ils été volés dans une voiture ou sur un véhicule ?",
      v.getVolDansVehicule());

    if (v.getObjetsVoles() != null && !v.getObjetsVoles().isEmpty()) {
      List<ObjetIncident> objets = v.getObjetsVoles();
      int n = objets.size();
      IntStream.range(0, n)
          .mapToObj(i -> Map.entry(i, objets.get(i)))
          .forEach(e -> {
            int i = e.getKey();
            ObjetIncident o = e.getValue();
            if (n > 1) {
              rows.add(new String[]{"Objet volé (" + (i + 1) + ")", ""});
            }
            addObjetIncident(rows, o);
            addObjetVol(rows, o);
          });
    }

    addBooleanOuiNonPdf(rows, "Avez-vous constaté des dégradations liées à ce vol ?", v.getAvezVousDegradation());

    addLieuEvent(rows, v, "du vol");
  }

  private void addObjetVol(List<String[]> rows, ObjetIncident o) {
    addIfNotNull(rows, "Numéro ou texte de gravure", o.getGravure());
    addIfNotNull(rows, "Valeur de l'objet (CHF)", o.getRealValue());
    addIfNotNull(rows, "Numéro de série", o.getNumeroSerie());
    addIfNotNull(rows, "Numéro de série inconnu", o.isNumeroSerieInconnu() ? "Oui" : null);
    addIfNotNull(rows, "Numéro IMEI (uniquement pour les téléphones portables)", o.getNumeroIMEI());
    addIfNotNull(rows, "Numéro IMEI inconnu", o.isNumeroIMEIInconnu() ? "Oui" : null);
    addIfNotNull(rows, "Justification de l'absence de numéro IMEI", o.getJustificationAbsenceIMEI());
  }

  private void addObjetIncident(List<String[]> rows, ObjetIncident o) {
    addIfNotNull(rows, "Type de l'objet", o.getTypeLabel());

    String marque = RIPOL_AUTRE.equals(o.getFabricantCode()) ? o.getFabricantAutre() : o.getFabricantLabel();
    addIfNotNull(rows, "Marque", marque);

    String modele = RIPOL_AUTRE.equals(o.getModeleCode()) ? o.getModeleAutre() : o.getModeleLabel();
    addIfNotNull(rows, "Modèle", modele);

    addIfNotNull(rows, "Couleur", o.getCouleurLabel());
    addIfNotNull(rows, "Couleur secondaire", o.getCouleurSecondaireLabel());
    addIfNotNull(rows, "Numéro de cadre", o.getNumeroCadre());
    addIfNotNull(rows, "Numéro de cadre inconnu", o.isNumeroCadreInconnu() ? "Oui" : null);
    addIfNotNull(rows, "Numéro de châssis (VIN)", o.getVin());
    addIfNotNull(rows, "Numéro de châssis (VIN) inconnu", o.isVinInconnu() ? "Oui" : null);
    addIfNotNull(rows, "Identifiant velofinder", o.getVelofinderId());
    addIfNotNull(rows, "Date d'achat", formatDate(o.getPurchaseDate()));
    addIfNotNull(rows, "Numéro de plaque", o.getPlaqueNumero());
    addIfNotNull(rows, "Numéro de plaque inconnu", o.isPlaqueInconnu() ? "Oui" : null);
    addIfNotNull(rows, "Pays de la plaque", o.getPlaquePaysLabel());
    addIfNotNull(rows, "Canton de la plaque", o.getPlaqueCantonLabel());
  }

  private void handleDommage(List<String[]> rows, DommageMateriel d) {
    addIfNotNull(rows, "Type de dommage",
      Optional.ofNullable(d.getTypeDommage())
        .map(TypeDommage::getLabel)
        .orElse(null));

    if (d.getObjetDegrades() != null && !d.getObjetDegrades().isEmpty()) {
      List<ObjetIncident> objets = d.getObjetDegrades();
      int n = objets.size();
      IntStream.range(0, n)
          .mapToObj(i -> Map.entry(i, objets.get(i)))
          .forEach(e -> {
            int i = e.getKey();
            ObjetIncident o = e.getValue();
            if (n > 1) {
              rows.add(new String[]{"Véhicule / objet endommagé (" + (i + 1) + ")", ""});
            }
            addObjetIncident(rows, o);
          });
    }

    addIfNotNull(rows, "Montant estimé",
      d.getMontantEstime() != null
        ? (d.getMontantEstime() + " " + safe(d.getDevise()))
        : null);
    addIfNotNull(rows, "Nature du dommage",
      joinList(Optional.ofNullable(d.getNaturesDommage())
        .orElse(List.of())
        .stream()
        .map(NatureDommage::getLabel)
        .toList()));
    addIfNotNull(rows, "Description du dommage", d.getDescription());
    addBooleanOuiNonPdf(rows, "Constat présent", d.getConstatPresent());
    addIfNotNull(rows, "Date du constat", formatDate(d.getDateConstat()));
    addLieuEvent(rows, d, "du dommage");
  }

  private void addLieuEvent(List<String[]> rows, IncidentBase details, String suffixeAdressePrincipale) {
    addBooleanOuiNonPdf(rows, "Adresse de la personne lesée ?", details.getAdresseLesee());
    addIfNotNull(rows, "Type de lieu",
      Optional.ofNullable(details.getTypeLieu())
        .map(RipolCode::label)
        .orElse(null));
    addBooleanOuiNonPdf(rows, "Connaissez-vous l'adresse exacte du vol ou du dommage ?",
      details.getAdresseConnue());

    if (Boolean.TRUE.equals(details.getIsTrajet())) {
      addAdresseEvenement("de départ", details.getAdresseIncident(), rows);
      addAdresseEvenement("d'arrivée", details.getAdresseIncidentSecondaire(), rows);
      return;
    }

    addAdresseEvenement(suffixeAdressePrincipale, details.getAdresseIncident(), rows);
  }

  private void handleCybercrime(List<String[]> rows, Cybercrime c) {
    addIfNotNull(rows, "Type de cybercrime",
      c.getTypeCybercrime() != null ? c.getTypeCybercrime().getLabel() : null);
    addIfNotNull(rows, "Description du cybercrime", c.getDescriptionCybercrime());
    addIfNotNull(rows, "Date premier contact", formatDate(c.getDatePremierContact()));
    addIfNotNull(rows, "Date dernier contact", formatDate(c.getDateDernierContact()));

    if (c.getCommandeFrauduleuse() != null) {
      CommandeFrauduleuse cf = c.getCommandeFrauduleuse();
      addIfNotNull(rows, "Prestataire", cf.getPrestataire());
      addIfNotNull(rows, "Date de découverte de l'escroquerie", formatDate(cf.getDateDecouverte()));
      addIfNotNull(rows, "Montant du délit",
        cf.getMontant() != null ? (cf.getMontant() + " CHF") : null);
      addBooleanOuiNonPdf(rows, "Assurance cybercrime disponible", cf.getAssurance());
      addBooleanOuiNonPdf(rows, "Courriel commande inconnu", cf.getEmailCommandeInconnu());
      addIfNotNull(rows, "Courriel de commande", cf.getEmailCommande());
      addBooleanOuiNonPdf(rows, "Téléphone commande inconnu", cf.getTelephoneCommandeInconnu());
      addIfNotNull(rows, "Téléphone de commande", cf.getTelephoneCommande());
      addBooleanOuiNonPdf(rows, "Livré à l'adresse du lesé", cf.getLivraisonAdresseLesee());
      addAdresseEvenement("(livraison)", cf.getAdresseLivraison(), rows);
    }

    if (c.getAchatNonRecu() != null) {
      AchatNonRecu a = c.getAchatNonRecu();
      addIfNotNull(rows, "Montant du délit (achat en ligne)", a.getMontantDelitAchatLigne());
      addIfNotNull(rows, "Article non livré (description)", a.getArticleNonLivreDescription());
      addIfNotNull(rows, "Prénom du vendeur", a.getPrenomVendeur());
      addIfNotNull(rows, "Nom du vendeur", a.getNomVendeur());
      addBooleanOuiNonPdf(rows, "Courriel vendeur inconnu", a.getEmailVendeurInconnu());
      addIfNotNull(rows, "Courriel du vendeur", a.getEmailVendeur());
      addBooleanOuiNonPdf(rows, "Téléphone vendeur inconnu", a.getTelephoneVendeurInconnu());
      addIfNotNull(rows, "Téléphone du vendeur", a.getTelephoneVendeur());
      addBooleanOuiNonPdf(rows, "Adresse vendeur inconnue", a.getAdresseVendeurInconnue());
      addAdresseEvenement("(vendeur)", a.getAdresseVendeur(), rows);
      addBooleanOuiNonPdf(rows, "Achat via place de marché", a.getAchatViaPlaceMarche());
      addIfNotNull(rows, "Plateforme utilisée",
        a.getPlateformeUtilisee() != null ? a.getPlateformeUtilisee().getLabel() : null);
      addIfNotNull(rows, "Plateforme (autre)", a.getPlateformeAutre());
      addIfNotNull(rows, "Identifiant / URL plateforme", a.getPlateformeId());
      addIfNotNull(rows, "Nom entreprise vendeur", a.getNomEntrepriseVendeur());
      addIfNotNull(rows, "Site web entreprise vendeur", a.getSiteWebEntrepriseVendeur());
      addBooleanOuiNonPdf(rows, "Document annonce indisponible", a.getAnnonceDocumentIndisponible());
      addIfNotNull(rows, "Raison absence annonce", a.getRaisonAbsenceAnnonce());
      addIfNotNull(rows, "Moyen de paiement",
        a.getMoyenPaiement() != null ? a.getMoyenPaiement().getLabel() : null);
      addIfNotNull(rows, "Moyen de paiement (autre)", a.getMoyenPaiementAutre());
      addIfNotNull(rows, "IBAN bénéficiaire", a.getIbanBeneficiaire());
      addIfNotNull(rows, "Compte PayPal bénéficiaire", a.getComptePaypalBeneficiaire());
      addIfNotNull(rows, "Twint bénéficiaire", a.getNumeroTwintBeneficiaire());
      addIfNotNull(rows, "Adresse wallet crypto", a.getAdresseWalletCrypto());
      addIfNotNull(rows, "Hash transaction crypto", a.getHashTransactionCrypto());
      addIfNotNull(rows, "Société bénéficiaire", a.getSocieteBeneficiaire());
      addIfNotNull(rows, "Nom du bénéficiaire", a.getNomBeneficiaire());
      addIfNotNull(rows, "Prénom du bénéficiaire", a.getPrenomBeneficiaire());
      addIfNotNull(rows, "Date de l’opération", formatDate(a.getDateOperation()));
      addBooleanOuiNonPdf(rows, "Preuve de paiement indisponible", a.getPreuvePaiementIndisponible());
      addIfNotNull(rows, "Raison absence preuve paiement", a.getRaisonAbsencePreuvePaiement());
      addBooleanOuiNonPdf(rows, "Copie identité transmise à l'auteur", a.getCopieIdentiteTransmiseAuteur());
      addBooleanOuiNonPdf(rows, "Copie identité auteur transmise", a.getCopieIdentiteAuteurTransmise());
    }

    if (c.getFausseAnnonce() != null) {
      FausseAnnonce f = c.getFausseAnnonce();
      addIfNotNull(rows, "URL complète", f.getUrlComplete());
      addIfNotNull(rows, "Titre de l’annonce", f.getTitreAnnonce());
      addIfNotNull(rows, "Nom du bailleur", f.getNomBailleur());
      addBooleanOuiNonPdf(rows, "Courriel bailleur inconnu", f.getEmailBailleurInconnu());
      addIfNotNull(rows, "Courriel du bailleur", f.getEmailBailleur());
      addBooleanOuiNonPdf(rows, "Téléphone bailleur inconnu", f.getTelephoneBailleurInconnu());
      addIfNotNull(rows, "Téléphone du bailleur", f.getTelephoneBailleur());
      addIfNotNull(rows, "Adresse du bien immobilier", f.getAdresseBienImmobilier());
      addIfNotNull(rows, "Montant demandé", f.getMontantDemande());
      addIfNotNull(rows, "Mode de paiement demandé", f.getModePaiementDemande());
    }
  }

  private void addSection(Document doc, String title, List<String[]> rows)
    throws DocumentException {

    Paragraph t = new Paragraph(title.toUpperCase(Locale.ROOT), SUBTITLE_FONT);
    t.setSpacingBefore(SECTION_SPACING_BEFORE);
    t.setSpacingAfter(SECTION_SPACING_AFTER);
    doc.add(t);

    PdfPTable table = new PdfPTable(SECTION_TABLE_COLUMN_COUNT);
    table.setWidthPercentage(TABLE_FULL_WIDTH_PERCENT);
    table.setWidths(new float[]{SECTION_COLUMN_LABEL_WIDTH, SECTION_COLUMN_VALUE_WIDTH});
    table.setSpacingAfter(TABLE_SPACING_AFTER);

    for (String[] row : rows) {
      table.addCell(createCell(row[0], true));
      table.addCell(createCell(row[1], false));
    }

    doc.add(table);
  }

  private PdfPCell createCell(String text, boolean bold) {
    PdfPCell cell = new PdfPCell(new Phrase(text, bold ? BOLD_FONT : NORMAL_FONT));
    cell.setGrayFill(1f);
    cell.setBorderColor(new GrayColor(BORDER_GREY));
    cell.setBorderWidth(CELL_BORDER_WIDTH);
    cell.setPadding(CELL_PADDING);
    return cell;
  }

  private void addBooleanOuiNonPdf(List<String[]> rows, String label, Boolean value) {
    if (value == null) {
      return;
    }
    rows.add(new String[]{label, value ? "Oui" : "Non"});
  }

  private void addIfNotNull(List<String[]> list, String label, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String s && s.isBlank()) {
      return;
    }
    list.add(new String[]{label, safe(String.valueOf(value))});
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private String joinList(List<String> list) {
    return (list == null || list.isEmpty()) ? null : String.join(", ", list);
  }

  private String formatDate(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }

    try {
      if (raw.contains("T")) {
        return LocalDateTime
          .parse(raw, INPUT_DATE_TIME)
          .toLocalDate()
          .format(DATE_FORMAT);
      }

      return LocalDate
        .parse(raw, INPUT_DATE)
        .format(DATE_FORMAT);

    } catch (Exception e) {
      return raw;
    }
  }

  private String formatEventDate(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }

    try {
      if (raw.contains("T")) {
        return LocalDateTime
          .parse(raw, INPUT_DATE_TIME)
          .format(DATE_TIME_FORMAT);
      }

      return LocalDate
        .parse(raw, INPUT_DATE)
        .format(DATE_FORMAT);

    } catch (Exception e) {
      return raw;
    }
  }

  private static class HeaderFooter extends PdfPageEventHelper {

    private static final int LOGO_SIZE = 38;
    private static final int TEXT_GREY = 90;
    private static final int FOOTER_FONT_SIZE = 7;
    private static final int STROKE_GREY = 120;
    private static final int HEADER_TABLE_COLUMN_COUNT = 3;
    private static final int FOOTER_TABLE_COLUMN_COUNT = 2;

    private static final float HEADER_HEIGHT = 45f;
    private static final float FOOTER_TEXT_HEIGHT = 30f;
    private static final float PAGE_NUMBER_HEIGHT = 25f;
    private static final float LINE_WIDTH = 0.5f;
    private static final float HEADER_COL_LOGO_WIDTH = 1.2f;
    private static final float HEADER_COL_LEFT_WIDTH = 5f;
    private static final float HEADER_COL_RIGHT_WIDTH = 6f;
    private static final float HEADER_TEXT_LEADING = 10f;
    private static final float FOOTER_COL_TEXT_WIDTH = 8f;
    private static final float FOOTER_COL_PAGE_WIDTH = 1f;
    private static final float FOOTER_TEXT_MULTIPLIED_LEADING = 1.4f;

    private static final double HEADER_X_OFFSET = 15d;
    private static final double HEADER_Y_OFFSET = 22d;
    private static final double HEADER_EXTRA_TOTAL_WIDTH = 30d;
    private static final double FOOTER_LINE_OFFSET = 45d;
    private static final double FOOTER_TABLE_OFFSET = 25d;
    private static final double FOOTER_Y_OFFSET = 5d;

    private final PrePlainte prePlainte;
    private Image logo;

    HeaderFooter(PrePlainte prePlainte) {
      this.prePlainte = prePlainte;
      try {
        ClassPathResource r = new ClassPathResource("static/logo-etat-geneve.png");
        if (r.exists()) {
          logo = Image.getInstance(r.getURL());
          logo.scaleToFit(LOGO_SIZE, LOGO_SIZE);
        }
      } catch (Exception e) {
        // Logo non bloquant : en cas d'erreur (ressource absente, image invalide),
        // le PDF est généré sans logo.
      }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      try {
        PdfContentByte cb = writer.getDirectContent();
        double width = (double) document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();

        Font greyFont = FontFactory.getFont(FontFactory.HELVETICA, FOOTER_FONT_SIZE);
        greyFont.setColor(TEXT_GREY, TEXT_GREY, TEXT_GREY);
        Font greyItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, FOOTER_FONT_SIZE);
        greyItalic.setColor(TEXT_GREY, TEXT_GREY, TEXT_GREY);

        PdfPTable header = new PdfPTable(HEADER_TABLE_COLUMN_COUNT);
        header.setTotalWidth((float) (width + HEADER_EXTRA_TOTAL_WIDTH));
        header.setWidths(new float[]{HEADER_COL_LOGO_WIDTH, HEADER_COL_LEFT_WIDTH , HEADER_COL_RIGHT_WIDTH});
        header.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setMinimumHeight(HEADER_HEIGHT);

        if (logo != null) {
          logoCell.addElement(logo);
        }
        header.addCell(logoCell);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);
        left.setMinimumHeight(HEADER_HEIGHT);

        Paragraph leftText = new Paragraph();
        leftText.setLeading(HEADER_TEXT_LEADING);

        leftText.add(new Chunk("REPUBLIQUE ET CANTON DE GENEVE\n", greyFont));
        leftText.add(new Chunk("Département des institutions et du numérique", greyFont));

        left.addElement(leftText);
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setMinimumHeight(HEADER_HEIGHT);

        Paragraph rightText = new Paragraph(
                "Pré-plainte " + prePlainte.getDemandeId()
                        + " enregistrée le "
                        + LocalDateTime.now().format(DATE_TIME_FORMAT),
                greyItalic
        );
        rightText.setAlignment(Element.ALIGN_RIGHT);

        right.addElement(rightText);
        header.addCell(right);

        double xPos = document.leftMargin() - HEADER_X_OFFSET;
        double yPos = document.getPageSize().getHeight() - HEADER_Y_OFFSET;

        header.writeSelectedRows(0, -1, (float) xPos, (float) yPos, cb);

        double footerY = document.bottomMargin() - FOOTER_Y_OFFSET;
        double x = (double) document.getPageSize().getWidth() - document.rightMargin();
        double y = footerY + FOOTER_LINE_OFFSET;

        cb.setLineWidth(LINE_WIDTH);
        cb.setRGBColorStroke(STROKE_GREY, STROKE_GREY, STROKE_GREY);
        cb.moveTo(document.leftMargin(), (float) y);
        cb.lineTo((float) x, (float) y);
        cb.stroke();

        PdfPTable footer = new PdfPTable(FOOTER_TABLE_COLUMN_COUNT);
        double totalWidth = (double) document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();

        footer.setTotalWidth((float) totalWidth);
        footer.setWidths(new float[]{FOOTER_COL_TEXT_WIDTH, FOOTER_COL_PAGE_WIDTH});
        footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        Paragraph footerText = new Paragraph();
        footerText.setLeading(0, FOOTER_TEXT_MULTIPLIED_LEADING);
        footerText.setAlignment(Element.ALIGN_CENTER);

        footerText.add(new Chunk("DIN • Rue de l'Hôtel-de-Ville 14 • 1204 Genève\n", greyFont));
        footerText.add(new Chunk("Tél. +41 (0) 22 546 54 44 • www.ge.ch\n", greyFont));
        footerText.add(new Chunk("Ligne TPG 92 - arrêt Bourg-de-Four", greyFont));

        PdfPCell textCell = new PdfPCell();
        textCell.addElement(footerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setMinimumHeight(FOOTER_TEXT_HEIGHT);

        PdfPCell pageCell = new PdfPCell(new Phrase(
          String.valueOf(writer.getPageNumber()),
          greyFont
        ));
        pageCell.setBorder(Rectangle.NO_BORDER);
        pageCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        pageCell.setMinimumHeight(PAGE_NUMBER_HEIGHT);

        footer.addCell(textCell);
        footer.addCell(pageCell);

        footer.writeSelectedRows(
          0,
          -1,
          document.leftMargin(),
          (float) (footerY + FOOTER_TABLE_OFFSET),
          cb
        );

      } catch (Exception e) {
        throw new ExceptionConverter(e);
      }
    }
  }

  public Fichier generateFilePdf(PrePlainte prePlainte) {
    String traceId = MDC.get(TRACE_ID);
    String demandeId = prePlainte.getDemandeId();
    try {
      byte[] pdf = generatePdf(prePlainte);

      Fichier pdfFichier = new Fichier(
        demandeId + "_" + LocalDate.now() + ".pdf",
        "application/pdf",
        Base64.getEncoder().encodeToString(pdf)
      );

      FichierValidator.validateFichier(pdfFichier);

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      String hash = HexFormat.of().formatHex(digest.digest(pdf));

      log.debug(
        "event={} traceId={} demandeId={} SHA-256={}",
        EVENT_GENERATION_HASH_PDF, traceId, demandeId, hash
      );

      log.info(
        "event={} traceId={} demandeId={}",
        EVENT_GENERATION_PDF_SUCCESS, traceId, demandeId
      );

      return pdfFichier;
    } catch (Exception e) {
      log.error(
        "event={} traceId={} demandeId={} error={}",
        EVENT_GENERATION_PDF_FAILURE, traceId, demandeId, e.getMessage(),
        e
      );
      throw new IllegalStateException("Erreur lors de la génération du PDF", e);
    }
  }
}
