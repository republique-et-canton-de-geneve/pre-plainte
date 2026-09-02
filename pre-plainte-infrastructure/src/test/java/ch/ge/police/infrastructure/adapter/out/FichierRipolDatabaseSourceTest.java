package ch.ge.police.infrastructure.adapter.out;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FichierRipolDatabaseSourceTest {

  @Test
  void ouvrirFlux_litLeFichierLocal() throws Exception {
    byte[] contenu = "SQLite format 3".getBytes(StandardCharsets.US_ASCII);
    Path fichier = Files.createTempFile("ripol-source-", ".db");
    fichier.toFile().deleteOnExit();
    Files.write(fichier, contenu);

    FichierRipolDatabaseSource source = new FichierRipolDatabaseSource(fichier.toString());

    try (InputStream flux = source.ouvrirFlux()) {
      assertArrayEquals(contenu, flux.readAllBytes());
    }
  }

  @Test
  void ouvrirFlux_whenCheminVide_throwsIllegalStateException() {
    FichierRipolDatabaseSource source = new FichierRipolDatabaseSource("   ");

    assertThrows(IllegalStateException.class, source::ouvrirFlux);
  }

  @Test
  void ouvrirFlux_whenFichierAbsent_throwsIllegalStateException() {
    FichierRipolDatabaseSource source = new FichierRipolDatabaseSource("does-not-exist.db");

    assertThrows(IllegalStateException.class, source::ouvrirFlux);
  }

  @Test
  void description_exposeLeChemin() {
    FichierRipolDatabaseSource source = new FichierRipolDatabaseSource("/tmp/dbppel3");

    assertEquals("fichier:/tmp/dbppel3", source.description());
  }
}
