<template>
  <div class="ff-wrapper" :class="wrapperClasses">
    <label v-if="label" class="ff-label">
      {{ label }}
      <span v-if="required" class="text-red-400">*</span>
    </label>
    <div class="ff-control">
      <slot />
    </div>
    <p v-if="help && !error" class="ff-help">{{ help }}</p>
    <p v-if="error" class="ff-error">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  label: { type: String, default: '' },
  help: { type: String, default: '' },
  error: { type: String, default: '' },
  required: { type: Boolean, default: false },
  dense: { type: Boolean, default: true },
})

const wrapperClasses = computed(() => [props.dense ? 'ff-dense' : ''])
</script>

<style scoped>
.ff-wrapper { @apply w-full; }
.ff-label { @apply block text-[10px] text-slate-300 mb-1; }
.ff-control { @apply w-full; }
.ff-help { @apply text-[10px] text-slate-400 mt-0.5; }
.ff-error { @apply text-[11px] text-red-400 mt-0.5; }
.ff-dense .ff-label { @apply mb-0.5; }
</style>
