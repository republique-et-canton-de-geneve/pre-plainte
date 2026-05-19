package ch.ge.police.core.domain.util;

import ch.ge.police.core.domain.exception.InvalidFichierException;
import ch.ge.police.core.domain.model.fichier.Fichier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class FichierValidatorTest {

  private static Fichier fichier(String name, byte[] content, String mime) {
    return new Fichier(
      name,
      mime,
      Base64.getEncoder().encodeToString(content)
    );
  }

  private static byte[] pdfContent() {
    return new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D };
  }

  private static byte[] pngContent() {
    return new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 };
  }

  private static byte[] jpegContent() {
    return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00 };
  }

  private static byte[] tiffLEContent() {
    return new byte[] { 0x49, 0x49, 0x2A, 0x00 };
  }

  @Test
  void should_accept_valid_pdf() {
    Fichier fichier = fichier("test.pdf", pdfContent(), "application/pdf");
    assertDoesNotThrow(() -> FichierValidator.validateFichiers(List.of(fichier)));
  }

  @Test
  void should_accept_multiple_valid_files_under_70mb() {
    Fichier pdf = fichier("a.pdf", pdfContent(), "application/pdf");
    Fichier png = fichier("b.png", pngContent(), "image/png");
    Fichier jpeg = fichier("c.jpg", jpegContent(), "image/jpeg");
    Fichier tiff = fichier("d.tif", tiffLEContent(), "image/tiff");

    assertDoesNotThrow(() ->
      FichierValidator.validateFichiers(List.of(pdf, png, jpeg, tiff))
    );
  }

  @Test
  void should_reject_invalid_extension() {
    Fichier fichier = fichier("test.exe", pdfContent(), "application/pdf");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertTrue(ex.getMessage().contains("Extension non autorisée"));
  }

  @Test
  void should_reject_corrupted_pdf() {
    byte[] corrupted = new byte[] { 0x00, 0x00, 0x00, 0x00 };
    Fichier fichier = fichier("test.pdf", corrupted, "application/pdf");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    assertThrows(
      InvalidFichierException.class,
      validation
    );
  }

  @Test
  void should_reject_file_over_10mb() {
    byte[] bigFile = new byte[(int) (10 * 1024 * 1024 + 1)];
    bigFile[0] = (byte) 0xFF;
    bigFile[1] = (byte) 0xD8;

    Fichier fichier = fichier("big.jpg", bigFile, "image/jpeg");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    assertThrows(
      InvalidFichierException.class,
      validation
    );
  }

  @Test
  void should_reject_total_size_over_70mb() {
    byte[] bigPdf = new byte[(int) (35 * 1024 * 1024)];
    bigPdf[0] = 0x25;
    bigPdf[1] = 0x50;
    bigPdf[2] = 0x44;
    bigPdf[3] = 0x46;

    Fichier f1 = fichier("a.pdf", bigPdf, "application/pdf");
    Fichier f2 = fichier("b.pdf", bigPdf, "application/pdf");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(f1, f2));

    assertThrows(
      InvalidFichierException.class,
      validation
    );
  }

  @Test
  void should_reject_invalid_base64() {
    Fichier fichier = new Fichier("test.pdf", "application/pdf", "###");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    assertThrows(
      InvalidFichierException.class,
      validation
    );
  }

  @Test
  void should_reject_when_total_size_exceeds_70mb() {
    byte[] nineMbPdf = new byte[9 * 1024 * 1024];
    nineMbPdf[0] = 0x25;
    nineMbPdf[1] = 0x50;
    nineMbPdf[2] = 0x44;
    nineMbPdf[3] = 0x46;

    List<Fichier> fichiers = IntStream.range(0, 8)
      .mapToObj(i -> fichier("file" + i + ".pdf", nineMbPdf, "application/pdf"))
      .toList();

    Executable validation = () ->
      FichierValidator.validateFichiers(fichiers);

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Taille totale des fichiers dépassée (max 70 Mo)",
      ex.getMessage()
    );
  }

  @Test
  void should_reject_file_with_null_name() {
    Fichier fichier = new Fichier(
      null,
      "application/pdf",
      Base64.getEncoder().encodeToString(pdfContent())
    );

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals("Nom du fichier manquant", ex.getMessage());
  }

  @Test
  void should_reject_file_with_null_base64_content() {
    Fichier fichier = new Fichier(
      "test.pdf",
      "application/pdf",
      null
    );

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Contenu Base64 manquant : test.pdf",
      ex.getMessage()
    );
  }

  @Test
  void should_reject_file_with_blank_base64_content() {
    Fichier fichier = new Fichier(
      "test.pdf",
      "application/pdf",
      "   "
    );

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Contenu Base64 manquant : test.pdf",
      ex.getMessage()
    );
  }

  @Test
  void should_reject_file_with_content_shorter_than_signature_length() {
    byte[] tooShort = new byte[] { 0x25, 0x50 }; // < 4

    Fichier fichier = fichier("test.pdf", tooShort, "application/pdf");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Fichier corrompu : test.pdf",
      ex.getMessage()
    );
  }

  @Test
  void should_reject_invalid_tiff_signature() {
    byte[] invalidTiff = new byte[] {
      0x00, 0x00, 0x2A, 0x00
    };

    Fichier fichier = fichier("image.tif", invalidTiff, "image/tiff");

    Executable validation = () ->
      FichierValidator.validateFichiers(List.of(fichier));

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Fichier TIFF invalide : image.tif",
      ex.getMessage()
    );
  }

  @Test
  void should_reject_when_number_of_files_exceeds_limit() {
    byte[] validPdf = new byte[] {
      0x25, 0x50, 0x44, 0x46, 0x2D
    };

    List<Fichier> fichiers = new ArrayList<>();

    for (int i = 0; i < 11; i++) {
      fichiers.add(
        fichier(
          "document" + i + ".pdf",
          validPdf,
          "application/pdf"
        )
      );
    }

    Executable validation = () ->
      FichierValidator.validateFichiers(fichiers);

    InvalidFichierException ex = assertThrows(
      InvalidFichierException.class,
      validation
    );

    assertEquals(
      "Nombre de fichiers dépassé (max 10)",
      ex.getMessage()
    );
  }

}
