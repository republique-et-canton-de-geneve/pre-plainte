package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;

/**
 * Classe utilitaire pour construire les références RIPOL de manière cohérente.
 * Centralise la création des objets RipolReference avec leur code, label et table source.
 */
public final class RipolReferenceBuilder {

  private RipolReferenceBuilder() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Crée une référence RIPOL standard avec source "RIPOL".
   */
  public static RipolReference of(String code, String label, String sourceTable) {
    if (code == null) {
      return null;
    }
    return RipolReference.of(code, label, sourceTable);
  }

  /**
   * Crée une référence RIPOL avec source "ISO" (pour les codes ISO comme le sexe).
   */
  public static RipolReference ofIso(String code, String label, String sourceTable) {
    if (code == null) {
      return null;
    }
    return RipolReference.ofIso(code, label, sourceTable);
  }
}
