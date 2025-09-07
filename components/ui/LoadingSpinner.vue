<template>
  <div v-if="overlay || fullscreen" v-show="show" :class="wrapperClass">
    <div class="flex flex-col items-center gap-2">
      <div :class="spinnerClass" aria-busy="true" aria-live="polite"></div>
      <div v-if="text" class="text-slate-300 text-xs">{{ text }}</div>
    </div>
  </div>
  <div v-else class="inline-flex items-center gap-2" v-show="show">
    <div :class="spinnerClass" aria-busy="true" aria-live="polite"></div>
    <span v-if="text" class="text-slate-300 text-xs">{{ text }}</span>
  </div>
  <slot v-if="!show && !overlay && !fullscreen" />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: true },
  size: { type: String, default: 'md' }, // xs|sm|md|lg
  text: { type: String, default: '' },
  overlay: { type: Boolean, default: false },
  fullscreen: { type: Boolean, default: false },
})

const sizeClass = computed(() => {
  switch (props.size) {
    case 'xs': return 'h-4 w-4 border-[2px]'
    case 'sm': return 'h-5 w-5 border-[2.5px]'
    case 'lg': return 'h-10 w-10 border-[3.5px]'
    default: return 'h-6 w-6 border-[3px]'
  }
})

const spinnerClass = computed(() => [
  'rounded-full animate-spin',
  'border-slate-600/60 border-t-orange-500',
  sizeClass.value
])

const wrapperClass = computed(() => [
  'inset-0 z-[80] flex items-center justify-center',
  props.fullscreen ? 'fixed bg-slate-900/60 backdrop-blur-[2px]' : 'absolute bg-slate-900/40'
])
</script>

<style scoped>
@keyframes spin { to { transform: rotate(360deg) } }
.animate-spin { animation: spin 0.9s linear infinite; }
</style>
