<template>
  <div>
    <h3 v-if="showTitle" class="text-h6 mb-3">{{ label }}</h3>
    <v-alert type="info" density="comfortable" variant="tonal" class="mb-4">
      {{ t("pieceJointe.contraintesUpload", { maxDocs: maxFilesAllowed, maxMo: maxFileSizeMo }) }}
    </v-alert>

    <v-file-input
      ref="fileInputRef"
      v-model="internalFiles"
      :label="label"
      :accept="accept"
      :multiple="multiple"
      class="d-none"
      @update:model-value="onFilesSelected"
    />

    <v-card
      class="confirmation-card pa-2 pa-md-6 mb-4 cursor-pointer"
      :elevation="
        isDragging
          ? VUETIFY_CARD_ELEVATION_DRAG_ACTIVE
          : isDarkMode
            ? VUETIFY_CARD_ELEVATION_DARK
            : VUETIFY_CARD_ELEVATION_DEFAULT
      "
      :variant="isDarkMode ? 'tonal' : 'flat'"
      @click="openFilePicker"
      @dragover.prevent="onDragOver"
      @dragleave.prevent="onDragLeave"
      @drop.prevent="onDrop"
    >
      <div class="d-flex flex-column align-center text-center">
        <v-icon icon="mdi-upload" size="34" class="mb-3 text-medium-emphasis" />

        <v-card-title class="text-h6 text-medium-emphasis pa-0 mb-4 text-wrap">
          {{ t("pieceJointe.zoneDepot") }}
        </v-card-title>

        <div class="d-flex align-center w-100 mb-4">
          <v-divider class="flex-grow-1" />
          <span class="text-body-2 text-medium-emphasis mx-4 text-no-wrap">
            {{ t("pieceJointe.ouSeparateur") }}
          </span>
          <v-divider class="flex-grow-1" />
        </div>

        <v-btn
          color="primary"
          variant="flat"
          rounded="pill"
          size="large"
          class="text-white"
          @click.stop="openFilePicker"
        >
          {{ t("pieceJointe.chargerFichiers") }}
          <template #append>
            <v-icon icon="mdi-upload" size="18" color="white" />
          </template>
        </v-btn>
      </div>
    </v-card>

    <v-card
      v-if="files.length > 0"
      class="confirmation-card pa-0 mb-2"
      :elevation="isDarkMode ? VUETIFY_CARD_ELEVATION_DARK : VUETIFY_CARD_ELEVATION_DEFAULT"
      :variant="isDarkMode ? 'tonal' : 'flat'"
    >
      <v-list lines="two" class="pa-0 bg-transparent" density="comfortable">
        <v-list-item
          v-for="(file, index) in files"
          :key="`file-${index}-${file.size ?? 0}-${displayFileName(file)}`"
          :title="displayFileName(file)"
          :subtitle="getFileFormatOrSize(file)"
        >
          <template #prepend>
            <v-avatar rounded="lg" size="40" color="grey-lighten-4">
              <v-icon icon="mdi-file-outline" color="grey-darken-1" />
            </v-avatar>
          </template>
          <template #append>
            <v-btn icon variant="text" @click="removeFile(index)">
              <v-icon icon="mdi-close" />
            </v-btn>
          </template>
        </v-list-item>
      </v-list>
    </v-card>

    <v-alert
      v-if="rejectedFiles.length > 0"
      type="error"
      closable
      density="comfortable"
      class="mt-2"
      @click:close="rejectedFiles = []"
    >
      <div>
        <span v-if="rejectedFiles.length === 1">{{ rejectedFiles[0] }}</span>
        <span v-else class="d-inline-flex align-center flex-wrap">
          {{ t("pieceJointe.erreursMultiples", { count: rejectedFiles.length }) }}
          <v-btn icon size="small" variant="text" class="ml-1" @click="showRejectedDetails = !showRejectedDetails">
            <v-icon>{{ showRejectedDetails ? "mdi-chevron-up" : "mdi-chevron-down" }}</v-icon>
          </v-btn>
        </span>

        <v-list v-if="showRejectedDetails" density="compact" class="bg-transparent pa-0 mt-2">
          <v-list-item v-for="(f, index) in rejectedFiles" :key="`${f}-${index}`" :title="f" />
        </v-list>
      </div>
    </v-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useTheme } from "vuetify";
import {
  BYTES_PER_MEGABYTE,
  MAX_FILE_SIZE,
  MAX_FILES,
  MAX_TOTAL_SIZE_70_MO,
  VALID_SIGNATURES_FILES,
  VUETIFY_CARD_ELEVATION_DARK,
  VUETIFY_CARD_ELEVATION_DEFAULT,
  VUETIFY_CARD_ELEVATION_DRAG_ACTIVE,
} from "@/constants/constant.ts";
const DEFAULT_ACCEPT_FILES = ".pdf,.jpg,.jpeg,.png,.tif";
const EMIT_UPDATE_MODEL_VALUE = "update:modelValue" as const;

const { t } = useI18n();
const theme = useTheme();
const isDarkMode = computed(() => theme.global.current.value.dark);

const props = withDefaults(
  defineProps<{
    modelValue?: File[];
    label: string;
    multiple?: boolean;
    maxFiles?: number;
    maxFileSize?: number;
    maxTotalSize?: number;
    accept?: string;
    showTitle?: boolean;
  }>(),
  {
    modelValue: () => [],
    multiple: true,
    maxFiles: MAX_FILES,
    maxFileSize: MAX_FILE_SIZE,
    maxTotalSize: MAX_TOTAL_SIZE_70_MO,
    accept: DEFAULT_ACCEPT_FILES,
    showTitle: true,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: File[]];
}>();

function normalizeFiles(value: unknown): File[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((item): item is File => item instanceof File && item.name.length > 0);
}

function filesAreSame(a: File[], b: File[]): boolean {
  if (a.length !== b.length) {
    return false;
  }
  return a.every((file, i) => file === b[i]);
}

const internalFiles = ref<File[]>([]);
const files = ref<File[]>(normalizeFiles(props.modelValue));
const rejectedFiles = ref<string[]>([]);
const showRejectedDetails = ref(false);
const fileInputRef = ref<any>(null);
const isDragging = ref(false);

const totalSize = computed(() => files.value.reduce((sum, f) => sum + f.size, 0));

const acceptedExtensions = computed(() =>
  props.accept
    .split(",")
    .map(ext => ext.trim().toLowerCase().replace(".", ""))
    .filter(Boolean),
);

const maxFileSizeMo = computed(() => Math.round(props.maxFileSize / BYTES_PER_MEGABYTE));
const maxFilesAllowed = computed(() => (props.multiple ? props.maxFiles : 1));

watch(files, newValue => {
  emit(EMIT_UPDATE_MODEL_VALUE, newValue);
});

watch(
  () => props.modelValue,
  value => {
    const normalized = normalizeFiles(value);
    if (!filesAreSame(files.value, normalized)) {
      files.value = normalized;
    }
    const raw = Array.isArray(value) ? value : [];
    if (normalized.length !== raw.length) {
      emit(EMIT_UPDATE_MODEL_VALUE, normalized);
    }
  },
  { deep: true, immediate: true },
);

function openFilePicker() {
  const input = fileInputRef.value?.$el?.querySelector('input[type="file"]') as HTMLInputElement | null;
  input?.click();
}

function onDragOver() {
  isDragging.value = true;
}

function onDragLeave() {
  isDragging.value = false;
}

function onDrop(event: DragEvent) {
  isDragging.value = false;
  const droppedFiles = event.dataTransfer?.files;
  if (!droppedFiles || droppedFiles.length === 0) {
    return;
  }
  onFilesSelected(Array.from(droppedFiles));
}

async function collectFilesFromSelection(selectedFiles: File[]): Promise<{ accepted: File[]; rejected: string[] }> {
  const accepted: File[] = [];
  const rejected: string[] = [];
  let currentTotal = totalSize.value;

  for (const file of selectedFiles) {
    if (files.value.length + accepted.length >= props.maxFiles) {
      rejected.push(t("pieceJointe.tropDeFichiers", { max: props.maxFiles }));
      break;
    }

    if (file instanceof File && file.name) {
      const fileNames = files.value.map(f => f.name);
      const uniqueName = generateUniqueName(file.name, fileNames);
      const fileToAdd = new File([file], uniqueName, { type: file.type });

      if (fileToAdd.size > props.maxFileSize) {
        rejected.push(t("pieceJointe.tailleMaxCustom", { nom: fileToAdd.name, max: maxFileSizeMo.value }));
      } else if (currentTotal + fileToAdd.size > props.maxTotalSize) {
        rejected.push(t("pieceJointe.tailleTotaleDepassee", { nom: fileToAdd.name }));
      } else if (!(await hasValidSignature(fileToAdd))) {
        rejected.push(t("pieceJointe.pdfInvalide", { nom: fileToAdd.name }));
      } else {
        accepted.push(fileToAdd);
        currentTotal += fileToAdd.size;
      }
    }
  }

  return { accepted, rejected };
}

async function onFilesSelected(value: File | File[] | null) {
  if (!value) {
    return;
  }

  const selectedFiles = Array.isArray(value) ? value : [value];
  const { accepted, rejected } = await collectFilesFromSelection(selectedFiles);
  rejectedFiles.value = rejected;

  if (props.multiple) {
    files.value = [...files.value, ...accepted];
  } else {
    files.value = accepted.slice(0, 1);
  }

  internalFiles.value = [];
}

function generateUniqueName(name: string, existing: string[]): string {
  if (!existing.includes(name)) {
    return name;
  }

  const [base, ext] = name.split(/(?=\.[^.]+$)/);
  let i = 1;
  let newName = `${base} (${i})${ext || ""}`;
  while (existing.includes(newName)) {
    i += 1;
    newName = `${base} (${i})${ext || ""}`;
  }
  return newName;
}

async function hasValidSignature(file: File): Promise<boolean> {
  try {
    const buffer = await file.slice(0, 4).arrayBuffer();
    const bytes = new Uint8Array(buffer);
    const extension = getExtension(file.name);

    if (!acceptedExtensions.value.includes(extension)) {
      return false;
    }

    switch (extension) {
      case "pdf":
        return match(bytes, VALID_SIGNATURES_FILES.pdf);
      case "png":
        return match(bytes, VALID_SIGNATURES_FILES.png);
      case "jpg":
      case "jpeg":
        return match(bytes, VALID_SIGNATURES_FILES.jpg);
      case "tif":
        return match(bytes, VALID_SIGNATURES_FILES.tif_le) || match(bytes, VALID_SIGNATURES_FILES.tif_be);
      default:
        return false;
    }
  } catch {
    return false;
  }
}

function getExtension(name: string | undefined | null): string {
  if (typeof name !== "string") {
    return "";
  }
  const idx = name.lastIndexOf(".");
  return idx === -1 ? "" : name.substring(idx + 1).toLowerCase();
}

function match(bytes: Uint8Array, signature: number[]): boolean {
  return signature.every((b, i) => bytes[i] === b);
}

function removeFile(index: number) {
  files.value.splice(index, 1);
}

function formatMo(size: number) {
  return (size / BYTES_PER_MEGABYTE).toFixed(2) + " Mo";
}

function displayFileName(file: File): string {
  return file.name ? file.name : t("pieceJointe.fichierSansNom");
}

function getFileFormatOrSize(file: File): string {
  const extension = getExtension(file.name);
  const size = file.size;
  return extension ? `${extension.toUpperCase()} - ${formatMo(size)}` : formatMo(size);
}
</script>
