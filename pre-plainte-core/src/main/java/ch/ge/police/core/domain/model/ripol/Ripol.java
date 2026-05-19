package ch.ge.police.core.domain.model.ripol;

/**
 * Représente un code RIPOL avec ses traductions.
 *
 * @param code       le code RIPOL
 * @param labelFr    le libellé en français
 * @param labelDe    le libellé en allemand
 * @param groupeType le type de groupe
 */
public record Ripol(
    String code,
    String labelFr,
    String labelDe,
    String groupeType
) {
}

