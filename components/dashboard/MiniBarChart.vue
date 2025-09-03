<template>
  <div class="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
    <div class="flex items-center justify-between mb-2">
      <div class="text-[12px] text-slate-400">{{ label }}</div>
      <div class="text-[12px] text-slate-500">{{ period }}</div>
    </div>
    <svg :width="width" :height="height" class="w-full">
      <g :transform="`translate(${padding}, ${padding})`">
        <template v-for="(v, i) in data" :key="i">
          <rect
            :x="i * (barW + gap)"
            :y="innerH - scale(v)"
            :width="barW"
            :height="scale(v)"
            :fill="barColor"
            rx="4"
          />
        </template>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, default: 'Volume de Propostas' },
  period: { type: String, default: 'Últimos 12 dias' },
  data: { type: Array, default: () => [] },
  color: { type: String, default: 'orange' }
})

const width = 480
const height = 140
const padding = 8
const innerW = width - padding * 2
const innerH = height - padding * 2
const gap = 6

const max = computed(() => Math.max(1, ...props.data))
const barW = computed(() => Math.max(6, (innerW - gap * (props.data.length - 1)) / Math.max(1, props.data.length)))
const scale = (v) => Math.round((v / (max.value || 1)) * innerH)
const barColor = computed(() => {
  switch (props.color) {
    case 'blue': return '#3b82f6'
    case 'green': return '#22c55e'
    case 'purple': return '#a855f7'
    default: return '#f97316'
  }
})
</script>

<style scoped>
svg { display: block; }
</style>

