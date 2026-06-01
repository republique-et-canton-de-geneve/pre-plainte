package ch.ge.police.infrastructure.ech051;

/**
 * Constantes utilisées pour la génération des documents eCH-0051.
 */
public final class Ech051Constants {

  private Ech051Constants() {
    throw new UnsupportedOperationException();
  }

  // === Constantes générales ===
  public static final String PROCESSING_STATUS_GREEN = "GREEN";
  /**
   * Dossier « individuel » : doit être distincte de toutes les {@code person/@key}
   * (ex. 4 lésé, 6 assureur cyber, 7 déclarant tiers ou accusé cyber, 3 assureur véhicule).
   */
  public static final String BUSINESS_CASE_KEY = "9";
  public static final String BUSINESS_CASE_KEY_TIERS = "10";
  public static final String EVENT_KEY = "1";
  
  // === Clés personnes ===
  public static final String PERSON_KEY_TIERS = "4";
  public static final String IDENTITY_KEY_TIERS = "3";
  public static final String PERSON_KEY_DECLARANT = "7";
  public static final String IDENTITY_KEY_DECLARANT = "6";
  public static final String PERSON_KEY_ASSURANCE_TIERS = "2";
  
  public static final String PERSON_KEY_VEHICLE = "5";
  public static final String IDENTITY_KEY_VEHICLE = "4";
  public static final String INSURER_KEY_VEHICLE = "3";
  
  public static final String PERSON_KEY_ORGANISATION = "4";
  public static final String PERSON_KEY_DECLARANT_ENTREPRISE = "6";
  public static final String IDENTITY_KEY_DECLARANT_ENTREPRISE = "5";
  public static final String PERSON_KEY_INFORMANT = "8";
  public static final String IDENTITY_KEY_INFORMANT = "7";
  public static final String PERSON_KEY_ASSURANCE_ENTREPRISE = "3";
  public static final String BUSINESS_CASE_KEY_ENTREPRISE = "10";
  
  // === Clés objets et véhicules ===
  public static final String OBJECT_KEY_TIERS = "5";
  public static final String OBJECT_KEY_CYBER_TRANSACTION = "10";
  public static final String VEHICLE_KEY = "2";
  public static final String INSURER_REF = "2";
  public static final String INSURER_REF_VEHICLE = "3";
  public static final String INSURER_REF_CYBER = "6";
  
  // === Langue ===
  public static final String DEFAULT_LANGUAGE = "fr";

  public static final String COUNTRY_UNKNOWN_RIPOL_CODE = "9999";
  public static final String COUNTRY_UNKNOWN_LABEL = "inconnu";

  // === Relations constants ===
  public static final String INVOLVEMENT_TYPE_VICTIM_CODE = "1";
  public static final String INVOLVEMENT_TYPE_VICTIM_LABEL = "lésé";
  public static final String INVOLVEMENT_TYPE_REPRESENTATIVE_CODE = "4";
  public static final String INVOLVEMENT_TYPE_REPRESENTATIVE_LABEL = "représentant";
  public static final String INVOLVEMENT_TYPE_INFORMANT_CODE = "7";
  public static final String INVOLVEMENT_TYPE_INFORMANT_LABEL = "pers. donnant des rens.";
  public static final String INVOLVEMENT_TYPE_ACCUSED_CODE = "U";
  public static final String INVOLVEMENT_TYPE_ACCUSED_LABEL = "Personne accusée";
  public static final String INVOLVEMENT_SOURCE_TABLE = "PERSONALIEN_ART";
  public static final String IDENTITY_CATEGORY_UNKNOWN = "U";
  
  public static final String OBJECT_ROLE_SEARCHED_CODE = "10";
  public static final String OBJECT_ROLE_SEARCHED_LABEL = "recherché";
  public static final String OBJECT_ROLE_SOURCE_TABLE = "CODE_SACHE";
  
  public static final String VEHICLE_ROLE_SEARCHED_CODE = "11";
  public static final String VEHICLE_ROLE_SEARCHED_LABEL = "véhicule/plaque recherché";
  public static final String VEHICLE_ROLE_SOURCE_TABLE = "CODE_SACHE";
  
  public static final String TYPE_OF_CRIME_VOL_CODE = "1000139080";
  public static final String TYPE_OF_CRIME_VOL_LABEL = "vol";
  public static final String TYPE_OF_CRIME_DOMMAGE_CODE = "1000144061";
  public static final String TYPE_OF_CRIME_DOMMAGE_LABEL = "dommages à la propriété";
  public static final String TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_CODE = "1000147050";
  public static final String TYPE_OF_CRIME_CYBER_COMMANDE_FRAUDULEUSE_LABEL = "utilisation frauduleuse d'un ordinateur";
  public static final String TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_CODE = "1000146003";
  public static final String TYPE_OF_CRIME_CYBER_FAUSSE_ANNONCE_LABEL = "escroquerie";
  public static final String TYPE_OF_CRIME_CYBER_ACHAT_NON_RECU_CODE = "1000146100";
  public static final String TYPE_OF_CRIME_CYBER_ACHAT_NON_RECU_LABEL = "escroquerie";
  public static final String TYPE_OF_OBJECT_CYBER_IDENTITY_CODE = "200300";
  public static final String TYPE_OF_OBJECT_CYBER_IDENTITY_LABEL = "carte d'identité";
  public static final String TYPE_OF_OBJECT_CYBER_IDENTITY_ID_TYPE = "IDPass";
  public static final String TYPE_OF_OBJECT_DOMMAGE_CODE = "200219";
  public static final String TYPE_OF_OBJECT_DOMMAGE_LABEL = "Dommage matériel";
  public static final String MODE_OPERANDI_DEGRADATIONS_CODE = "4000236";
  public static final String MODE_OPERANDI_DEGRADATIONS_LABEL = "vandalisme";
  public static final String MODE_OPERANDI_TAGS_CODE = "4000900";
  public static final String MODE_OPERANDI_TAGS_LABEL = "dessiner/marquer des signes";
  public static final String MODE_OPERANDI_CYBER_COMMANDE_FRAUDULEUSE_CODE = "6166537";
  public static final String MODE_OPERANDI_CYBER_COMMANDE_FRAUDULEUSE_LABEL = "cyber-escroquerie: utilisation frauduleuse des moyens de paiement numériques";
  public static final String MODE_OPERANDI_CYBER_FAUSSE_ANNONCE_CODE = "6163016";
  public static final String MODE_OPERANDI_CYBER_FAUSSE_ANNONCE_LABEL = "cyber-escroquerie: fausse annonce immobilière";
  public static final String MODE_OPERANDI_CYBER_ACHAT_NON_RECU_CODE = "6166530";
  public static final String MODE_OPERANDI_CYBER_ACHAT_NON_RECU_LABEL = "cyber-escroquerie : non-livraison sur des sites de petites annonces (acheteur abusé par une fausse annonce)";

  // === Tables sources RIPOL ===
  public static final class RipolSourceTables {
    private RipolSourceTables() {}

    public static final String SEXE = "ISO5218";
    public static final String NATIONALITE = "EXT_GPNATI";
    public static final String LIEU_ORIGINE = "EXT_GDE_HEIMATORT";
    public static final String TYPE_OBJET = "sacheBezeichnung";
    public static final String TYPE_OBJET_DK_ZAHL = "DK_ZAHL";
    public static final String OBJET_MARQUE = "sacheMarke";
    public static final String OBJET_MODELE = "sacheModell";
    public static final String OBJET_COULEUR = "sacheFarbe";
    public static final String TYPE_VEHICULE = "ART_FZ";
    public static final String COULEUR_VEHICULE = "FARBE_FZ";
    public static final String TYPE_LIEU = "OERTLICHKEIT";
    public static final String MODUS_OPERANDI = "MODUS_OPERANDI";
    public static final String TYPE_CRIME = "STRAFB_HANDL";
    public static final String PLAQUE_PAYS = "EXT_GPNATI";
    public static final String PLAQUE_CANTON = "SCHILD";
  }

  // === Types de messages ===
  public static final class MessageTypes {
    private MessageTypes() {}

    public static final String VOL = "5357";
    public static final String VELO_MOFA = "5350";
    public static final String DOMMAGE = "5352";
    public static final String CYBER_COMMANDE_FRAUDULEUSE = "5388";
    public static final String CYBER_FAUSSE_ANNONCE = "5389";
    public static final String CYBER_ACHAT_NON_RECU = "5387";
  }

  // === Source IDs pour ProcessData ===
  public static final class SourceIds {
    private SourceIds() {}

    public static final String VOL = "Fahrrad- und Mofadiebstahl";
    public static final String DOMMAGE_MATERIEL = "Sachbeschädigungen";
    public static final String CYBERCRIME = "cybercrime";
    public static final String CYBERCRIME_COMMANDE_FRAUDULEUSE = "Meine Daten wurden für eine Bestellung missbraucht";
    public static final String CYBERCRIME_FAUSSE_ANNONCE = "Falsches Wohnungsinserat/falsche Immobilienanzeige";
    public static final String CYBERCRIME_ACHAT_NON_RECU = "Online-Kauf bezahlt, aber keine Ware erhalten";
    public static final String UNKNOWN = "unknown";
  }

  // === Labels communication ===
  public static final class CommunicationUsageLabels {
    private CommunicationUsageLabels() {}

    public static final String EMAIL = "e-mail privé";
    public static final String EMAIL_CODE = "7";
    public static final String MOBILE = "téléphone mobile";
    public static final String MOBILE_CODE = "4";
    public static final String PHONE = "téléphone fixe";
    public static final String PHONE_CODE = "1";
    public static final String URI = "quality_assurance_link";
    public static final String SOURCE_TABLE = "ART_TEL_FAX";
  }
}
