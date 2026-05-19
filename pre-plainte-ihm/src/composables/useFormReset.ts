import { watch } from "vue";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";

/**
 * Composable pour gérer la réinitialisation automatique des formulaires
 * quand le store est réinitialisé
 */
export function useFormReset(
  form: { resetForm: (options: { values: PrePlainteFormFields }) => void },
  resetCondition: (data: PrePlainteFormFields) => boolean,
  onReset?: () => void,
) {
  const store = useCreatePrePlainteStore();

  watch(
    () => store.userFormData,
    newData => {
      const isReset = resetCondition(newData);

      if (isReset) {
        form.resetForm({ values: newData });
        onReset?.();
      }
    },
    { deep: true },
  );
}

/**
 * Conditions de reset prédéfinies pour les différents formulaires
 */
export const resetConditions = {
  personalInfo: (data: PrePlainteFormFields) => {
    const hasNoMainFields = !data.nom && !data.prenom && !data.email && !data.lienAvecPersonne;
    const hasNoTiersFields = !data.tiersNom && !data.tiersPrenom && !data.tiersEmail;
    const hasNoPersonneMoraleFields = !data.organisationNom && !data.organisationEmail;
    return hasNoMainFields && hasNoTiersFields && hasNoPersonneMoraleFields;
  },

  eventInfo: (data: PrePlainteFormFields) => !data.typeIncident && !data.dateDebutEvenement && !data.dateFinEvenement,

  global: (data: PrePlainteFormFields) =>
    Object.values(data).every(
      value => value === "" || value === null || value === undefined || (Array.isArray(value) && value.length === 0),
    ),
};
