package ch.ge.police.core.domain.util;

import ch.ge.police.core.domain.exception.InvalidFichierException;
import ch.ge.police.core.domain.model.fichier.Fichier;

import java.util.Base64;
import java.util.List;
import java.util.Locale;

public final class FichierValidator {

  private FichierValidator() {}

  private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;   // 10 Mo
  private static final long MAX_TOTAL_SIZE = 70L * 1024 * 1024;  // 70 Mo
  private static final int MAX_FILES = 10;

  private static final List<String> ALLOWED_EXTENSIONS =
    List.of("jpg", "jpeg", "png", "pdf", "tif");

  private static final byte[] PDF_SIGNATURE = { 0x25, 0x50, 0x44, 0x46 };

  private static final byte[] PNG_SIGNATURE = {
    (byte) 0x89, 0x50, 0x4E, 0x47
  };

  private static final byte[] JPEG_SIGNATURE = {
    (byte) 0xFF, (byte) 0xD8
  };

  private static final byte TIFF_LE = 0x49;
  private static final byte TIFF_BE = 0x4D;

  private static final int FIRST_BYTE_INDEX = 0;
  private static final int SECOND_BYTE_INDEX = 1;
  private static final int MIN_SIGNATURE_LENGTH = 4;
  private static final char EXTENSION_SEPARATOR = '.';

  public static void validateFichiers(List<Fichier> fichiers) {
    if (fichiers == null || fichiers.isEmpty()) return;

    if (fichiers.size() > MAX_FILES) {
      throw new InvalidFichierException("Nombre de fichiers dépassé (max " + MAX_FILES + ")");
    }

    long totalSize = 0;

    for (Fichier fichier : fichiers) {
      byte[] content = validateFichier(fichier);
      totalSize += content.length;
    }

    if (totalSize > MAX_TOTAL_SIZE) {
      throw new InvalidFichierException(
        "Taille totale des fichiers dépassée (max 70 Mo)"
      );
    }
  }

  public static byte[] validateFichier(Fichier fichier) {
    validateNom(fichier);
    validateExtension(fichier);

    byte[] content = decodeBase64(fichier);
    validateFileSize(content, fichier.nom());
    validateSignature(content, fichier);
    return content;
  }

  private static void validateNom(Fichier fichier) {
    if (fichier.nom() == null || fichier.nom().isBlank()) {
      throw new InvalidFichierException("Nom du fichier manquant");
    }
  }

  private static void validateExtension(Fichier fichier) {
    String extension = extractExtension(fichier.nom());
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new InvalidFichierException(
        "Extension non autorisée : " + fichier.nom()
      );
    }
  }

  private static byte[] decodeBase64(Fichier fichier) {
    if (fichier.contenuBase64() == null || fichier.contenuBase64().isBlank()) {
      throw new InvalidFichierException(
        "Contenu Base64 manquant : " + fichier.nom()
      );
    }

    try {
      return Base64.getDecoder().decode(fichier.contenuBase64());
    } catch (IllegalArgumentException e) {
      throw new InvalidFichierException(
        "Contenu Base64 invalide : " + fichier.nom(), e
      );
    }
  }

  private static void validateFileSize(byte[] content, String filename) {
    if (content.length > MAX_FILE_SIZE) {
      throw new InvalidFichierException(
        "Fichier trop volumineux (max 10 Mo) : " + filename
      );
    }
  }

  private static void validateSignature(byte[] content, Fichier fichier) {
    if (content.length < MIN_SIGNATURE_LENGTH) {
      throw new InvalidFichierException(
        "Fichier corrompu : " + fichier.nom()
      );
    }

    String extension = extractExtension(fichier.nom());

    switch (extension) {
      case "pdf" -> validateSignature(content, PDF_SIGNATURE, fichier.nom());
      case "png" -> validateSignature(content, PNG_SIGNATURE, fichier.nom());
      case "jpg", "jpeg" -> validateSignature(content, JPEG_SIGNATURE, fichier.nom());
      case "tif" -> validateTiffSignature(content, fichier.nom());
      default ->
        /* Ce bloc ne devrait jamais être atteint car les extensions sont déjà filtrées par la liste autorisée. */
        throw new InvalidFichierException(
          "Type de fichier non supporté : " + fichier.nom()
        );
    }
  }

  private static void validateSignature(byte[] content, byte[] signature, String filename) {
    for (int i = 0; i < signature.length; i++) {
      if (content[i] != signature[i]) {
        throw new InvalidFichierException(
          "Fichier invalide ou corrompu : " + filename
        );
      }
    }
  }

  private static void validateTiffSignature(byte[] content, String filename) {
    boolean valid =
      (content[FIRST_BYTE_INDEX] == TIFF_LE && content[SECOND_BYTE_INDEX] == TIFF_LE) ||
        (content[FIRST_BYTE_INDEX] == TIFF_BE && content[SECOND_BYTE_INDEX] == TIFF_BE);

    if (!valid) {
      throw new InvalidFichierException(
        "Fichier TIFF invalide : " + filename
      );
    }
  }

  private static String extractExtension(String filename) {
    int idx = filename.lastIndexOf(EXTENSION_SEPARATOR);
    return (idx > FIRST_BYTE_INDEX) ? filename.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
  }

}
