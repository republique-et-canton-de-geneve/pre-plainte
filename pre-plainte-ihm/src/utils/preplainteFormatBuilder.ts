import type { PrePlainteDTO } from "@/types/preplainte-payload-interface";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { PersonneMapper, IncidentMapper, ReverseMapper } from "@/utils/mappers";
import type { CreneauRendezVous } from "@/types/rendez-vous-interface.ts";
import { GENRE_LABEL_KEYS } from "@/constants/constant.ts";
import { i18n } from "@/common/i18n";

/**
 * Construit la structure de données pour le backend avec sous-objets :
 * - informationsPersonnelles.tiers (sous-objet avec adresse) si applicable
 * - informationsPersonnelles.organisation (sous-objet avec adresse) si applicable
 * - incident.details (sous-objet)
 */
export const buildPrePlainteForBackend = async (form: PrePlainteFormFields): Promise<PrePlainteDTO> => {
  const informationsPersonnelles = await PersonneMapper.buildInformationsPersonnelles(form);

  if (!form.typeIncident?.trim()) {
    return { informationsPersonnelles };
  }

  const details = await IncidentMapper.buildIncidentDetails(form);
  return {
    informationsPersonnelles,
    incident: {
      typeIncident: details.typeIncident,
      details: details as any, // Cast temporaire pour compatibilité avec IncidentDetailsDTO
    },
  };
};

/**
 * Convertit un DTO backend vers un formulaire frontend (BROUILLON)
 */
export function preplainteDtoToForm(dto: PrePlainteDTO, base: PrePlainteFormFields): PrePlainteFormFields {
  return ReverseMapper.dtoToForm(dto, base);
}

/**
 * Construit la structure de données pour la génération du PDF
 */
export const buildPrePlainteForGenerationPdf = async (
  demandeId: string | null,
  form: PrePlainteFormFields,
  creneauRendezVous?: CreneauRendezVous | null,
): Promise<PrePlainteDTO> => {
  const informationsPersonnelles = await PersonneMapper.buildInformationsPersonnelles(form);

  const t = i18n.global.t;

  if (informationsPersonnelles?.genre) {
    const key = GENRE_LABEL_KEYS[informationsPersonnelles.genre.code];
    informationsPersonnelles.genre = {
      ...informationsPersonnelles.genre,
      label: key ? t(key) : informationsPersonnelles.genre.label,
    };
  }

  if (informationsPersonnelles?.tiers?.genre) {
    const key = GENRE_LABEL_KEYS[informationsPersonnelles.tiers.genre.code];
    informationsPersonnelles.tiers.genre = {
      ...informationsPersonnelles.tiers.genre,
      label: key ? t(key) : informationsPersonnelles.tiers.genre.label,
    };
  }

  if (!form.typeIncident?.trim()) {
    return { informationsPersonnelles };
  }

  const details = await IncidentMapper.buildIncidentDetails(form);
  return {
    demandeId,
    informationsPersonnelles,
    incident: {
      typeIncident: details.typeIncident,
      details: details as any,
    },
    creneauRendezVous,
  };
};
