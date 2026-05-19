<template>
  <div>
    <v-form @submit.prevent="onSubmit">
      <h1 class="mb-5 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
      <v-card class="pa-2 pa-md-6 mb-4">
        <h2 class="pre-plainte-main-card-title mb-4 text-h2 text-md-h1 mt-4">
          {{ t("informationsPersonnelles.titre") }}
        </h2>
        <AccessibleVSelect
          :label="t('informationsPersonnelles.lienAvecPersonne')"
          v-model="lienAvecPersonne"
          :items="liensAvecPersonneOptions"
          item-title="label"
          item-value="value"
          :error-messages="lienAvecPersonneError"
          class="mb-4"
          variant="outlined"
          :hint="t('informationsPersonnelles.hintLienAvecPersonne')"
          persistent-hint
          prepend-inner-icon="mdi-check-circle-outline"
        />
        <TiersRepresentationForm v-if="showTypeRepresentation" />
        <EntrepriseRepresentationForm v-if="showPosteOuFonction" />
        <InfosPersonnellesForm />

        <div v-if="!showTiersSection && !showOrganisationSection" class="d-md-none mt-4">
          <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-4">
            <v-btn variant="outlined" color="primary" class="w-100" @click="$emit('cancel')">
              {{ t("common.precedent") }}
            </v-btn>
            <v-btn type="submit" variant="flat" color="primary" class="w-100" @click="onSubmit">
              {{ t("common.continuer") }}
            </v-btn>
          </div>
          <div class="d-flex justify-center">
            <v-btn variant="plain" color="primary" class="pa-0" @click="onSave">
              {{ t("common.sauvegarder") }}
            </v-btn>
          </div>
        </div>
      </v-card>
      <InfosTiersForm v-if="showTiersSection">
        <template #buttons>
          <div class="d-md-none mt-4">
            <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-4">
              <v-btn variant="outlined" color="primary" class="w-100" @click="$emit('cancel')">
                {{ t("common.precedent") }}
              </v-btn>
              <v-btn type="submit" variant="flat" color="primary" class="w-100" @click="onSubmit">
                {{ t("common.continuer") }}
              </v-btn>
            </div>
            <div class="d-flex justify-center">
              <v-btn variant="plain" color="primary" class="pa-0" @click="onSave">
                {{ t("common.sauvegarder") }}
              </v-btn>
            </div>
          </div>
        </template>
      </InfosTiersForm>

      <InfosOrganisationForm v-if="showOrganisationSection">
        <template #buttons>
          <div class="d-md-none mt-4">
            <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-4">
              <v-btn variant="outlined" color="primary" class="w-100" @click="$emit('cancel')">
                {{ t("common.precedent") }}
              </v-btn>
              <v-btn type="submit" variant="flat" color="primary" class="w-100" @click="onSubmit">
                {{ t("common.continuer") }}
              </v-btn>
            </div>
            <div class="d-flex justify-center">
              <v-btn variant="plain" color="primary" class="pa-0" @click="onSave">
                {{ t("common.sauvegarder") }}
              </v-btn>
            </div>
          </div>
        </template>
      </InfosOrganisationForm>

      <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
        <ExitActionsForm :is-mobile="true" />
      </div>

      <v-row class="mt-4 d-none d-md-flex" align="center">
        <v-col cols="12" md="auto" class="d-flex">
          <v-btn variant="plain" color="primary" @click="onSave">
            {{ t("common.sauvegarder") }}
          </v-btn>
        </v-col>
        <v-spacer />
        <v-col cols="12" md="auto" class="d-flex justify-end">
          <v-btn variant="outlined" color="primary" class="me-4" @click="$emit('cancel')">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn type="submit" variant="flat" color="primary">
            {{ t("common.poursuivre") }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>
  </div>
</template>

<script setup lang="ts">
import { LIENS_AVEC_PERSONNE } from "@/constants/constant";
import { createInfosPersonnellesSchema } from "@/schemas/infos-personnelles.schema.ts";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { toTypedSchema } from "@vee-validate/zod";
import { useField, useForm } from "vee-validate";
import { computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll";
import { useFormReset, resetConditions } from "@/composables/useFormReset";
import TiersRepresentationForm from "@/components/pre-plainte-component/personal-info/TiersRepresentationForm.vue";
import EntrepriseRepresentationForm from "@/components/pre-plainte-component/personal-info/EntrepriseRepresentationForm.vue";
import InfosTiersForm from "@/components/pre-plainte-component/personal-info/InfosTiersForm.vue";
import InfosPersonnellesForm from "@/components/pre-plainte-component/personal-info/InfosPersonnellesForm.vue";
import InfosOrganisationForm from "@/components/pre-plainte-component/personal-info/InfosOrganisationForm.vue";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";

const { t, locale } = useI18n();
const emit = defineEmits<{ cancel: []; continue: []; save: [] }>();
const store = useCreatePrePlainteStore();
const { scrollToFirstValidationError } = useFormErrorScroll();

const LIEN_TIERS = "TIERS";
const LIEN_ENTREPRISE = "ENTREPRISE";

const liensAvecPersonneOptions = computed(() =>
  LIENS_AVEC_PERSONNE.map(item => ({
    value: item.value,
    label: t(item.labelKey),
  })),
);

const validationSchema = computed(() => {
  return toTypedSchema(createInfosPersonnellesSchema(t));
});

const form = useForm<PrePlainteFormFields>({
  initialValues: store.userFormData,
  validationSchema,
});

watch(locale, () => {
  form.validate();
});

const { value: lienAvecPersonne, errorMessage: lienAvecPersonneError } = useField("lienAvecPersonne");

const showTiersSection = computed(() => lienAvecPersonne.value === LIEN_TIERS);
const showOrganisationSection = computed(() => lienAvecPersonne.value === LIEN_ENTREPRISE);
const showTypeRepresentation = computed(() => lienAvecPersonne.value === LIEN_TIERS);
const showPosteOuFonction = computed(() => lienAvecPersonne.value === LIEN_ENTREPRISE);
const { handleSubmit } = form;

useFormReset(form, resetConditions.personalInfo);

const onSubmit = handleSubmit(
  formValues => {
    store.setUserFormData(formValues);
    emit("continue");
  },
  errors => {
    scrollToFirstValidationError(errors);
  },
);

const onSave = () => {
  store.setUserFormData(form.values);
  emit("save");
};
</script>

<style scoped>
@media (max-width: 959px) {
  .button-container-mobile {
    margin-left: -16px;
    margin-right: -16px;
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
