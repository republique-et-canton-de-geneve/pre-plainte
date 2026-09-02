package ch.ge.police.infrastructure.adapter.out;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "ripol.db.source", havingValue = "fichier")
public class FichierRipolDatabaseSource implements RipolDatabaseSource {

  private static final String TRACE_ID = "traceId";

  private final String chemin;

  public FichierRipolDatabaseSource(@Value("${ripol.db.file.path:}") String chemin) {
    this.chemin = chemin;
  }

  @Override
  public InputStream ouvrirFlux() throws IOException {
    if (chemin.isBlank()) {
      throw new IllegalStateException(
        "La propriété ripol.db.file.path doit être renseignée lorsque ripol.db.source=fichier."
      );
    }

    Path fichier = Path.of(chemin);
    if (!Files.isReadable(fichier)) {
      throw new IllegalStateException(
        "Base RIPOL introuvable ou illisible : " + fichier.toAbsolutePath()
      );
    }

    log.info(
      "event=ripol_db_fichier_local traceId={} path={}",
      MDC.get(TRACE_ID),
      fichier.toAbsolutePath()
    );

    return Files.newInputStream(fichier);
  }

  @Override
  public String description() {
    return "fichier:" + chemin;
  }
}
