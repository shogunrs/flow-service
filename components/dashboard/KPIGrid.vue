<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
    <KPIStat
      v-for="(k, i) in items"
      :key="k.key || i"
      :label="k.label"
      :value="k.value"
      :delta="k.delta"
      :hint="k.hint"
      :icon="k.icon"
      :color="k.color"
    />
  </div>
  
</template>

<script setup>
import KPIStat from './KPIStat.vue'

defineProps({ items: { type: Array, default: () => [] } })
</script>

<style scoped>
</style>

