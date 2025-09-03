<template>
  <div :class="gridClasses">
    <slot />
  </div>
</template>

<script setup>
const props = withDefaults(defineProps<{ cols?: number; gap?: string }>(), {
  cols: 12,
  gap: 'gap-2'
})

const gridClasses = computed(() => [
  'grid',
  props.gap,
  `grid-cols-${props.cols}`
])
</script>

<style scoped>
/* Utility wrapper for consistent grid usage */
</style>

