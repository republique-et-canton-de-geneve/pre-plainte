<template>
  <v-snackbar
    v-model="visible"
    :timeout="3500"
    color="primary"
    location="bottom end"
    class="app-snackbar-bottom-end"
  >
    {{ message }}
  </v-snackbar>
</template>

<script setup lang="ts">
import { ref } from "vue";

const visible = ref(false);
const message = ref("");

const show = (text: string) => {
  message.value = text;
  visible.value = true;
};

defineExpose({ show });
</script>

<style scoped>
.app-snackbar-bottom-end {
  margin: 0 16px 16px 0;
}
</style>
