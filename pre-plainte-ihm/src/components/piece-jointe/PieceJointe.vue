<template>
  <div :data-field="fieldName || undefined">
    <div v-if="showTitle" class="mb-3">
      <h3 class="text-h6 font-weight-bold mb-1">{{ displayLabel }}</h3>
      <div v-if="subtitle" class="text-body-2 text-medium-emphasis">
        {{ subtitle }}
      </div>
    </div>
    <v-alert type="info" density="comfortable" variant="tonal" class="mb-4">
      {{ t("pieceJointe.contraintesUpload", { maxDocs: maxFilesAllowed, maxMo: maxFileSizeMo }) }}
    </v-alert>

    <v-file-input
      ref="fileInputRef"
      v-model="internalFiles"
      :label="displayLabel"
      :accept="accept"
      :multiple="multiple"
      class="d-none"
      @update:model-value="onFilesSelected"
    />
    <v-file-input
      ref="cameraInputRef"
      v-model="cameraFiles"
      accept="image/*"
      capture="environment"
      class="d-none"
      @update:model-value="onCameraSelected"
    />

    <v-card
      class="confirmation-card pa-2 pa-md-6 mb-4"
      :class="{ 'cursor-pointer': !isUploading }"
      :elevation="
        isDragging
          ? VUETIFY_CARD_ELEVATION_DRAG_ACTIVE
          : isDarkMode
            ? VUETIFY_CARD_ELEVATION_DARK
            : VUETIFY_CARD_ELEVATION_DEFAULT
      "
      :variant="isDarkMode ? 'tonal' : 'flat'"
      @click="!isUploading && openFilePicker()"
      @dragover.prevent="onDragOver"
      @dragleave.prevent="onDragLeave"
      @drop.prevent="onDrop"
    >
      <div class="d-flex flex-column align-center text-center">
        <v-icon icon="mdi-upload" size="34" class="mb-3 text-medium-emphasis" />

        <v-card-title class="text-h6 text-medium-emphasis pa-0 mb-4 text-wrap">
          {{ t("pieceJointe.zoneDepot") }}
        </v-card-title>

        <div v-if="isUploading" class="w-100 mb-4 px-2 px-md-8">
          <div class="text-body-2 text-medium-emphasis mb-2">
            {{ t("pieceJointe.chargementGlobal", { current: uploadCompletedCount, total: uploadTotalCount }) }}
          </div>
          <v-progress-linear
            :model-value="overallUploadProgress"
            color="primary"
            height="8"
            rounded
          />
        </div>

        <div class="d-flex align-center w-100 mb-4">
          <v-divider class="flex-grow-1" />
          <span class="text-body-2 text-medium-emphasis mx-4 text-no-wrap">
            {{ t("pieceJointe.ouSeparateur") }}
          </span>
          <v-divider class="flex-grow-1" />
        </div>

        <div class="d-flex flex-column flex-sm-row align-center ga-3">
          <v-btn
            color="primary"
            variant="flat"
            rounded="pill"
            size="large"
            class="text-white"
            :disabled="isUploading"
            :loading="isUploading"
            @click.stop="openFilePicker"
          >
            {{ t("pieceJointe.chargerFichiers") }}
            <template #append>
              <v-icon icon="mdi-upload" size="18" color="white" />
            </template>
          </v-btn>
          <v-btn
            v-if="mobile"
            color="primary"
            variant="outlined"
            rounded="pill"
            size="large"
            :disabled="isUploading"
            @click.stop="openCameraPicker"
          >
            {{ t("pieceJointe.prendrePhoto") }}
            <template #append>
              <v-icon icon="mdi-camera" size="18" />
            </template>
          </v-btn>
        </div>
        <div v-if="files.length > 0" class="text-body-2 text-medium-emphasis mt-4">
          {{ t("pieceJointe.espaceUtilise", { used: usedSizeMo, max: maxTotalSizeMo }) }}
        </div>
      </div>
    </v-card>

    <v-card
      v-if="uploadItems.length > 0"
      class="confirmation-card pa-0 mb-2"
      :elevation="isDarkMode ? VUETIFY_CARD_ELEVATION_DARK : VUETIFY_CARD_ELEVATION_DEFAULT"
      :variant="isDarkMode ? 'tonal' : 'flat'"
    >
      <v-list lines="two" class="pa-0 bg-transparent" density="comfortable">
        <v-list-item
          v-for="item in uploadItems"
          :key="item.id"
          :title="item.name"
          :subtitle="t('pieceJointe.chargementEnCours', { progress: item.progress })"
        >
          <template #prepend>
            <v-avatar rounded="lg" size="40" color="primary" variant="tonal">
              <v-icon icon="mdi-upload" color="primary" />
            </v-avatar>
          </template>
          <template #append>
            <div class="recap-upload-progress">
              <v-progress-linear
                :model-value="item.progress"
                color="primary"
                height="6"
                rounded
                class="mb-1"
              />
            </div>
          </template>
        </v-list-item>
      </v-list>
    </v-card>

    <div v-if="files.length > 0" class="d-flex flex-column ga-1 mb-2">
      <v-card
        v-for="(file, index) in files"
        :key="`file-${index}-${file.size ?? 0}-${displayFileName(file)}`"
        class="confirmation-card pa-2"
        :elevation="isDarkMode ? VUETIFY_CARD_ELEVATION_DARK : VUETIFY_CARD_ELEVATION_DEFAULT"
        :variant="isDarkMode ? 'tonal' : 'flat'"
      >
        <div class="piece-jointe-file-row">
          <div class="piece-jointe-file-main">
            <button
              v-if="isImageFile(file) && previewUrls[fileKey(file)]"
              type="button"
              class="piece-jointe-thumb"
              :aria-label="t('pieceJointe.apercu')"
              @click="openPreview(file)"
            >
              <img
                :src="previewUrls[fileKey(file)]"
                :alt="displayFileName(file)"
                class="piece-jointe-thumb__img"
              />
            </button>
            <button
              v-else-if="isPdfFile(file)"
              type="button"
              class="piece-jointe-file-btn"
              :aria-label="t('pieceJointe.apercu')"
              @click="openPreview(file)"
            >
              <v-icon
                class="piece-jointe-file-icon"
                size="28"
                icon="mdi-file-pdf-box"
                color="error"
              />
            </button>
            <v-icon
              v-else
              class="piece-jointe-file-icon"
              size="28"
              icon="mdi-file-outline"
              color="grey-darken-1"
            />
            <div class="piece-jointe-file-meta">
              <div class="text-body-2 font-weight-medium piece-jointe-file-name">
                {{ displayFileName(file) }}
              </div>
              <div class="text-caption text-medium-emphasis">
                {{ getFileFormatOrSize(file) }}
              </div>
            </div>
          </div>
          <div class="piece-jointe-file-actions">
            <v-btn
              v-if="isImageFile(file) || isPdfFile(file)"
              class="d-none d-sm-inline-flex"
              variant="text"
              color="primary"
              size="small"
              @click="openPreview(file)"
            >
              {{ t("pieceJointe.apercu") }}
            </v-btn>
            <v-btn
              v-if="isImageFile(file) || isPdfFile(file)"
              class="d-sm-none"
              icon
              variant="text"
              color="primary"
              size="small"
              :aria-label="t('pieceJointe.apercu')"
              @click="openPreview(file)"
            >
              <v-icon icon="mdi-eye-outline" size="20" />
            </v-btn>
            <v-btn
              icon
              variant="text"
              size="small"
              :disabled="isUploading"
              :aria-label="t('pieceJointe.supprimer')"
              @click="askRemoveFile(index)"
            >
              <v-icon icon="mdi-close" size="20" />
            </v-btn>
          </div>
        </div>
      </v-card>
    </div>

    <v-dialog v-model="previewOpen" max-width="960">
      <v-sheet v-if="previewFile" class="pa-4">
        <div class="d-flex align-start justify-space-between ga-3 mb-3">
          <span class="text-subtitle-1 piece-jointe-preview-title">{{ displayFileName(previewFile) }}</span>
          <v-btn icon="mdi-close" variant="text" class="flex-shrink-0" @click="previewOpen = false" />
        </div>
        <img
          v-if="isImageFile(previewFile) && previewUrls[fileKey(previewFile)]"
          :src="previewUrls[fileKey(previewFile)]"
          :alt="displayFileName(previewFile)"
          class="piece-jointe-preview-img"
        />
        <iframe
          v-else-if="isPdfFile(previewFile) && previewUrls[fileKey(previewFile)]"
          :src="previewUrls[fileKey(previewFile)]"
          class="piece-jointe-preview-pdf"
          :title="displayFileName(previewFile)"
        />
      </v-sheet>
    </v-dialog>

    <v-dialog v-model="deleteDialogOpen" max-width="480">
      <v-card>
        <v-card-title class="text-h6">{{ t("pieceJointe.supprimerTitre") }}</v-card-title>
        <v-card-text>
          {{ t("pieceJointe.supprimerMessage", { nom: pendingDeleteName }) }}
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click="cancelRemoveFile">{{ t("common.annuler") }}</v-btn>
          <v-btn color="error" variant="flat" @click="confirmRemoveFile">{{ t("pieceJointe.supprimer") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-alert
      v-if="errorMessage"
      type="error"
      density="comfortable"
      class="mt-2"
      :text="errorMessage"
    />

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
import { ref, watch, computed, onUnmounted, reactive } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay, useTheme } from "vuetify";
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
import { requiredLabel } from "@/utils/helpers/labelHelpers";
const DEFAULT_ACCEPT_FILES = ".pdf,.jpg,.jpeg,.png,.tif";
const EMIT_UPDATE_MODEL_VALUE = "update:modelValue" as const;

const { t } = useI18n();
const theme = useTheme();
const { mobile } = useDisplay();
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
    required?: boolean;
    subtitle?: string;
    errorMessages?: string | string[] | null;
    fieldName?: string | null;
  }>(),
  {
    modelValue: () => [],
    multiple: true,
    maxFiles: MAX_FILES,
    maxFileSize: MAX_FILE_SIZE,
    maxTotalSize: MAX_TOTAL_SIZE_70_MO,
    accept: DEFAULT_ACCEPT_FILES,
    showTitle: true,
    required: false,
    subtitle: "",
    errorMessages: null,
    fieldName: null,
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
const cameraInputRef = ref<any>(null);
const cameraFiles = ref<File[]>([]);
const isDragging = ref(false);
const isUploading = ref(false);
const uploadItems = ref<Array<{ id: string; name: string; progress: number }>>([]);
const uploadCompletedCount = ref(0);
const uploadTotalCount = ref(0);
const previewUrls = reactive<Record<string, string>>({});
const previewOpen = ref(false);
const previewFile = ref<File | null>(null);
const deleteDialogOpen = ref(false);
const pendingDeleteIndex = ref<number | null>(null);

const totalSize = computed(() => files.value.reduce((sum, f) => sum + f.size, 0));
const usedSizeMo = computed(() => (totalSize.value / BYTES_PER_MEGABYTE).toFixed(1));
const maxTotalSizeMo = computed(() => Math.round(props.maxTotalSize / BYTES_PER_MEGABYTE));
const overallUploadProgress = computed(() => {
  if (uploadTotalCount.value === 0) {
    return 0;
  }
  if (uploadItems.value.length === 0) {
    return 100;
  }
  const sum = uploadItems.value.reduce((acc, item) => acc + item.progress, 0);
  return Math.round(sum / uploadTotalCount.value);
});

const acceptedExtensions = computed(() =>
  props.accept
    .split(",")
    .map(ext => ext.trim().toLowerCase().replace(".", ""))
    .filter(Boolean),
);

const maxFileSizeMo = computed(() => Math.round(props.maxFileSize / BYTES_PER_MEGABYTE));
const maxFilesAllowed = computed(() => (props.multiple ? props.maxFiles : 1));
const displayLabel = computed(() => (props.required ? requiredLabel(props.label) : props.label));
const errorMessage = computed(() => {
  if (!props.errorMessages) {
    return "";
  }
  return Array.isArray(props.errorMessages) ? (props.errorMessages[0] ?? "") : props.errorMessages;
});

const pendingDeleteName = computed(() => {
  if (pendingDeleteIndex.value === null) {
    return "";
  }
  const file = files.value[pendingDeleteIndex.value];
  return file ? displayFileName(file) : "";
});

watch(
  files,
  newValue => {
    emit(EMIT_UPDATE_MODEL_VALUE, newValue);
    syncPreviewUrls(newValue);
  },
  { immediate: true },
);

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

function fileKey(file: File): string {
  return `${file.name}-${file.size}-${file.lastModified}`;
}

function isImageFile(file: File): boolean {
  const type = (file.type || "").toLowerCase();
  const name = file.name.toLowerCase();
  return type.startsWith("image/") || /\.(jpe?g|png|tif|tiff|webp|gif)$/i.test(name);
}

function isPdfFile(file: File): boolean {
  const type = (file.type || "").toLowerCase();
  return type === "application/pdf" || file.name.toLowerCase().endsWith(".pdf");
}

function syncPreviewUrls(list: File[]) {
  const nextKeys = new Set(list.map(fileKey));
  for (const key of Object.keys(previewUrls)) {
    if (!nextKeys.has(key)) {
      URL.revokeObjectURL(previewUrls[key]);
      delete previewUrls[key];
    }
  }
  for (const file of list) {
    const key = fileKey(file);
    if ((isImageFile(file) || isPdfFile(file)) && !previewUrls[key]) {
      previewUrls[key] = URL.createObjectURL(file);
    }
  }
}

function openPreview(file: File) {
  if (!isImageFile(file) && !isPdfFile(file)) {
    return;
  }
  if (!previewUrls[fileKey(file)]) {
    previewUrls[fileKey(file)] = URL.createObjectURL(file);
  }
  previewFile.value = file;
  previewOpen.value = true;
}

function askRemoveFile(index: number) {
  pendingDeleteIndex.value = index;
  deleteDialogOpen.value = true;
}

function cancelRemoveFile() {
  pendingDeleteIndex.value = null;
  deleteDialogOpen.value = false;
}

function confirmRemoveFile() {
  if (pendingDeleteIndex.value !== null) {
    removeFile(pendingDeleteIndex.value);
  }
  cancelRemoveFile();
}

function openFilePicker() {
  if (isUploading.value) {
    return;
  }
  const input = fileInputRef.value?.$el?.querySelector('input[type="file"]') as HTMLInputElement | null;
  input?.click();
}

function openCameraPicker() {
  if (isUploading.value) {
    return;
  }
  const input = cameraInputRef.value?.$el?.querySelector('input[type="file"]') as HTMLInputElement | null;
  input?.click();
}

function onCameraSelected(value: File | File[] | null) {
  let selected: File[] = [];
  if (Array.isArray(value)) {
    selected = value;
  } else if (value) {
    selected = [value];
  }
  cameraFiles.value = [];
  if (selected.length > 0) {
    onFilesSelected(selected);
  }
}

function onDragOver() {
  isDragging.value = true;
}

function onDragLeave() {
  isDragging.value = false;
}

function onDrop(event: DragEvent) {
  if (isUploading.value) {
    return;
  }
  isDragging.value = false;
  const droppedFiles = event.dataTransfer?.files;
  if (!droppedFiles || droppedFiles.length === 0) {
    return;
  }
  onFilesSelected(Array.from(droppedFiles));
}

function wait(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

const PROGRESS_STEP_MS = 220;
const PROGRESS_HOLD_MS = 800;

async function setItemProgress(id: string, progress: number) {
  const item = uploadItems.value.find(entry => entry.id === id);
  if (item) {
    item.progress = Math.min(100, Math.max(0, progress));
  }
  await wait(PROGRESS_STEP_MS);
}

async function collectFilesFromSelection(selectedFiles: File[]): Promise<{ accepted: File[]; rejected: string[] }> {
  const accepted: File[] = [];
  const rejected: string[] = [];
  let currentTotal = totalSize.value;

  uploadTotalCount.value = selectedFiles.length;
  uploadCompletedCount.value = 0;
  uploadItems.value = selectedFiles.map((file, index) => ({
    id: `${file.name}-${file.size}-${file.lastModified}-${index}`,
    name: file.name || t("pieceJointe.fichierSansNom"),
    progress: 0,
  }));

  for (let index = 0; index < selectedFiles.length; index++) {
    const file = selectedFiles[index];
    const itemId = uploadItems.value[index]?.id;
    if (!file || !itemId) {
      continue;
    }

    await setItemProgress(itemId, 15);

    if (files.value.length + accepted.length >= props.maxFiles) {
      rejected.push(t("pieceJointe.tropDeFichiers", { max: props.maxFiles }));
      await setItemProgress(itemId, 100);
      uploadCompletedCount.value += 1;
      break;
    }

    if (!(file instanceof File) || !file.name) {
      await setItemProgress(itemId, 100);
      uploadCompletedCount.value += 1;
      continue;
    }

    const fileNames = files.value.map(f => f.name).concat(accepted.map(f => f.name));
    const uniqueName = generateUniqueName(file.name, fileNames);
    const fileToAdd = new File([file], uniqueName, { type: file.type });
    await setItemProgress(itemId, 40);

    if (fileToAdd.size > props.maxFileSize) {
      rejected.push(t("pieceJointe.tailleMaxCustom", { nom: fileToAdd.name, max: maxFileSizeMo.value }));
      await setItemProgress(itemId, 100);
      uploadCompletedCount.value += 1;
      continue;
    }

    if (currentTotal + fileToAdd.size > props.maxTotalSize) {
      rejected.push(t("pieceJointe.tailleTotaleDepassee", { nom: fileToAdd.name }));
      await setItemProgress(itemId, 100);
      uploadCompletedCount.value += 1;
      continue;
    }

    await setItemProgress(itemId, 70);
    if (await hasValidSignature(fileToAdd)) {
      accepted.push(fileToAdd);
      currentTotal += fileToAdd.size;
      await setItemProgress(itemId, 100);
    } else {
      rejected.push(t("pieceJointe.pdfInvalide", { nom: fileToAdd.name }));
      await setItemProgress(itemId, 100);
    }
    uploadCompletedCount.value += 1;
  }

  return { accepted, rejected };
}

async function onFilesSelected(value: File | File[] | null) {
  if (!value || isUploading.value) {
    return;
  }

  const selectedFiles = Array.isArray(value) ? value : [value];
  if (selectedFiles.length === 0) {
    return;
  }

  isUploading.value = true;
  try {
    const { accepted, rejected } = await collectFilesFromSelection(selectedFiles);
    rejectedFiles.value = rejected;

    if (props.multiple) {
      files.value = [...files.value, ...accepted];
    } else {
      files.value = accepted.slice(0, 1);
    }
  } finally {
    await wait(PROGRESS_HOLD_MS);
    uploadItems.value = [];
    uploadCompletedCount.value = 0;
    uploadTotalCount.value = 0;
    isUploading.value = false;
    internalFiles.value = [];
  }
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
  files.value = files.value.filter((_, i) => i !== index);
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

onUnmounted(() => {
  for (const key of Object.keys(previewUrls)) {
    URL.revokeObjectURL(previewUrls[key]);
    delete previewUrls[key];
  }
});
</script>

<style scoped>
.recap-upload-progress {
  width: 120px;
}

.piece-jointe-file-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.piece-jointe-file-main {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex: 1 1 auto;
  min-width: 0;
}

.piece-jointe-file-meta {
  min-width: 0;
  flex: 1 1 auto;
}

.piece-jointe-file-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.piece-jointe-thumb {
  display: block;
  width: 40px;
  height: 40px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: transparent;
  flex-shrink: 0;
}

.piece-jointe-thumb__img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.piece-jointe-file-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
}

.piece-jointe-file-icon {
  opacity: 1 !important;
  flex-shrink: 0;
}

.piece-jointe-file-name {
  overflow-wrap: anywhere;
  word-break: break-word;
  line-height: 1.3;
}

.piece-jointe-preview-title {
  min-width: 0;
  flex: 1 1 auto;
  overflow-wrap: anywhere;
  word-break: break-word;
  line-height: 1.35;
  padding-top: 8px;
}

.piece-jointe-preview-img {
  display: block;
  max-width: 100%;
  max-height: 70vh;
  margin: 0 auto;
  object-fit: contain;
}

.piece-jointe-preview-pdf {
  width: 100%;
  height: 70vh;
  border: 0;
  border-radius: 8px;
  background: rgb(var(--v-theme-surface));
}
</style>
