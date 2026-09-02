package ch.ge.police.infrastructure.adapter.out;

import java.io.IOException;
import java.io.InputStream;

public interface RipolDatabaseSource {

  InputStream ouvrirFlux() throws IOException;

  String description();
}
