<template>
  <h1 class="mb-4 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>

  <v-card class="pa-2 pa-md-6">
    <h2 class="pre-plainte-main-card-title text-h4 text-md-h2 mb-3 mb-md-4" style="font-weight: 500">
      {{ cardTitle }}
    </h2>

    <v-alert type="success" class="mb-6 mb-md-10" :icon="mobile ? false : undefined">
      <div v-if="customMessage">
        {{ customMessage }}
      </div>
      <div v-else-if="rendezVousMessage">
        {{ rendezVousMessage }}
      </div>
      <div v-else>
        {{ t("submission.succes") }}
      </div>
    </v-alert>

    <div class="d-md-none d-flex flex-column gap-2">
      <v-btn variant="outlined" color="primary" class="w-100 mb-2" @click="$emit('restart')">
        {{ t("common.refaireDemande") }}
      </v-btn>
      <v-btn
        variant="outlined"
        color="primary"
        class="w-100 mb-2"
        :disabled="!userFormData.selectedCreneau"
        @click="telechargerIcs"
      >
        {{ t("common.ajouterAgenda") }}
      </v-btn>
      <v-btn variant="flat" color="primary" append-icon="mdi-download" class="w-100 mb-2" @click="telechargerPdf">
        {{ t("common.telechargerPdf") }}
      </v-btn>
    </div>

    <div class="d-none d-md-flex">
      <v-btn variant="outlined" color="primary" @click="$emit('restart')">
        {{ t("common.refaireDemande") }}
      </v-btn>
      <v-spacer />
      <v-btn
        variant="outlined"
        color="primary"
        class="mr-4"
        :disabled="!userFormData.selectedCreneau"
        @click="telechargerIcs"
      >
        {{ t("common.ajouterAgenda") }}
      </v-btn>
      <v-btn variant="flat" color="primary" append-icon="mdi-download" class="mr-4" @click="telechargerPdf">
        {{ t("common.telechargerPdf") }}
      </v-btn>
    </div>
  </v-card>

  <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
    <ExitActionsForm :is-mobile="true" />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { storeToRefs } from "pinia";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { downloadPrePlaintePdf } from "@/services/prePlaintePdfService";
import { computed } from "vue";
import { useDisplay } from "vuetify";
import { buildPrePlainteForGenerationPdf } from "@/utils/preplainteFormatBuilder.ts";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";

const { t } = useI18n();
const { mobile } = useDisplay();
defineEmits<{ restart: [] }>();
const props = defineProps<{ customMessage?: string; customTitle?: string }>();

const store = useCreatePrePlainteStore();
const { userFormData } = storeToRefs(store);

const telechargerPdf = async () => {
  try {
    const pdfData = await buildPrePlainteForGenerationPdf(store.demandeId, store.userFormData, userFormData.value.selectedCreneau);
    await downloadPrePlaintePdf(pdfData);
  } catch (e) {
    console.error(t("common.erreurTelechargementPdf"), e);
  }
};

const formatDateTimeFrench = (dateString?: string, includeTime = false) => {
  if (!dateString) {
    return "";
  }
  try {
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric",
    };
    let formatted = date.toLocaleDateString("fr-FR", options);
    formatted = formatted.charAt(0).toUpperCase() + formatted.slice(1);

    if (includeTime) {
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      formatted += ` à ${hours}h${minutes}`;
    }

    return formatted;
  } catch {
    return dateString;
  }
};

const rendezVousMessage = computed(() => {
  const data = userFormData.value;

  if (data.selectedCreneau) {
    const creneau = data.selectedCreneau;
    const codeRdv = data.codeRdv || "—";
    const nouvelleDateChoisi = formatDateTimeFrench(creneau.date);

    return t("modification.messageConfirmation", {
      date: nouvelleDateChoisi,
      heureDebut: creneau.heureDebut,
      heureFin: creneau.heureFin,
      lieu: creneau.lieu,
      code: codeRdv,
    });
  }

  return null;
});

const customMessage = computed(() => props.customMessage || "");

const cardTitle = computed(() =>
  props.customTitle || t("submission.formulaireComplet")
);

const pad2 = (n: number) => n.toString().padStart(2, "0");

const toICSDateUTC = (d: Date) =>
  `${d.getUTCFullYear()}${pad2(d.getUTCMonth() + 1)}${pad2(d.getUTCDate())}T${pad2(d.getUTCHours())}${pad2(d.getUTCMinutes())}${pad2(d.getUTCSeconds())}Z`;

const combineDateAndTime = (dateLike: string, hhmm: string) => {
  const base = new Date(dateLike);
  const [hh, mm] = hhmm.split(":").map(x => Number.parseInt(x, 10));
  return new Date(base.getFullYear(), base.getMonth(), base.getDate(), hh || 0, mm || 0, 0);
};

const escapeICS = (value: string) =>
  value
    .replaceAll("\\", String.raw`\\`)
    .replaceAll("\n", String.raw`\n`)
    .replaceAll(",", String.raw`\,`)
    .replaceAll(";", String.raw`\;`);

const buildIcsContent = () => {
  const data = userFormData.value;
  const creneau = data.selectedCreneau;
  if (!creneau) {
    return null;
  }

  const startLocal = combineDateAndTime(creneau.date, creneau.heureDebut);
  const endLocal = combineDateAndTime(creneau.date, creneau.heureFin);

  const uid = `${store.demandeId || "preplainte"}-${startLocal.getTime()}@preplainte`;
  const dtstamp = toICSDateUTC(new Date());

  const summary = escapeICS(t("submission.rdvPreplainte"));
  const location = escapeICS(creneau.lieu || "");
  const code = escapeICS(data.codeRdv || "");
  const description = escapeICS(code ? `${t("submission.codeRdv")} : ${code}` : t("submission.rdvPreplainte"));

  const dtstart = toICSDateUTC(new Date(startLocal));
  const dtend = toICSDateUTC(new Date(endLocal));

  const lines = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//GE Police//PrePlainte//FR",
    "CALSCALE:GREGORIAN",
    "METHOD:PUBLISH",
    "BEGIN:VEVENT",
    `UID:${uid}`,
    `DTSTAMP:${dtstamp}`,
    `DTSTART:${dtstart}`,
    `DTEND:${dtend}`,
    `SUMMARY:${summary}`,
    location ? `LOCATION:${location}` : "",
    `DESCRIPTION:${description}`,
    "END:VEVENT",
    "END:VCALENDAR",
  ].filter(Boolean);

  return lines.join("\r\n") + "\r\n";
};

const telechargerIcs = () => {
  try {
    const ics = buildIcsContent();
    if (!ics) {
      return;
    }

    const filename = `rendez-vous-preplainte-${store.demandeId || "rdv"}.ics`;
    const blob = new Blob([ics], { type: "text/calendar;charset=utf-8" });

    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Erreur lors du téléchargement du fichier agenda (.ics)", e);
  }
};
</script>
