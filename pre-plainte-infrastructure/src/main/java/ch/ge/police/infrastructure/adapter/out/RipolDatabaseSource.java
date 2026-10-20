package ch.ge.police.infrastructure.adapter.out;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public interface RipolDatabaseSource {

  InputStream ouvrirFlux() throws IOException;

  String description();

  default Optional<Path> cheminLocal() {
    return Optional.empty();
  }
}
