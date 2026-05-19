package ch.ge.police.core.domain.model.common;

/**
 * Représente une sélection RIPOL avec son code et son libellé.
 * Utilisé pour stocker les codes RIPOL dynamiques de la base de données.
 *
 * @param code  le code RIPOL (ex: "713103" pour téléphone mobile)
 * @param label le libellé affiché à l'utilisateur
 */
public record RipolCode(
    String code,
    String label
) {
  public boolean hasCode() {
    return code != null && !code.isBlank();
  }
}

