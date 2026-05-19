<template>
  <div class="full-width-skeleton">
    <v-skeleton-loader
      type="card"
      class="mb-4"
      :loading="true"
    >
      <v-card class="pa-2 pa-md-6">
        <v-skeleton-loader
          type="heading"
          class="mb-4"
        />
        <v-skeleton-loader
          type="text"
          class="mb-3"
        />
        <v-skeleton-loader
          type="text"
          class="mb-3"
        />
        <v-skeleton-loader
          type="text"
          class="mb-3"
        />
      </v-card>
    </v-skeleton-loader>

    <v-skeleton-loader
      type="card"
      class="mb-4"
      :loading="true"
    >
      <v-card class="pa-2 pa-md-6">
        <v-skeleton-loader
          type="heading"
          class="mb-4"
        />
        <v-skeleton-loader
          type="text"
          class="mb-3"
        />
        <v-skeleton-loader
          type="text"
          class="mb-3"
        />
      </v-card>
    </v-skeleton-loader>

    <div class="loading-actions">
      <v-skeleton-loader
        type="button"
        class="mr-4"
      />
      <v-skeleton-loader
        type="button"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  step?: number;
}>();
</script>

<style scoped>
.full-width-skeleton {
  min-height: 400px;
  width: 100%;
}

.loading-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 2rem;
}
</style>
