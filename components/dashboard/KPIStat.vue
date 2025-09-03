<template>
  <div
    class="rounded-xl border border-slate-800 bg-slate-900/60 p-4 shadow-sm hover:shadow transition-colors"
  >
    <div class="flex items-center justify-between">
      <div>
        <div class="text-[12px] text-slate-400">{{ label }}</div>
        <div class="mt-1 text-xl font-semibold text-white">{{ value }}</div>
      </div>
      <div
        class="w-10 h-10 rounded-lg flex items-center justify-center"
        :class="iconBg"
      >
        <i :class="[icon, 'text-white']"></i>
      </div>
    </div>
    <div class="mt-2 text-[12px]" :class="deltaClass">
      <i :class="delta >= 0 ? 'fa-solid fa-arrow-up-right' : 'fa-solid fa-arrow-down-right'"></i>
      <span class="ml-1">{{ deltaPrefix }}{{ Math.abs(delta) }}%</span>
      <span class="ml-2 text-slate-400">{{ hint }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  delta: { type: Number, default: 0 },
  hint: { type: String, default: 'vs. mês anterior' },
  icon: { type: String, default: 'fa-solid fa-circle' },
  color: { type: String, default: 'orange' } // orange|blue|green|purple
})

const deltaClass = computed(() => (props.delta >= 0 ? 'text-green-300' : 'text-red-300'))
const deltaPrefix = computed(() => (props.delta >= 0 ? '+' : '-'))
const iconBg = computed(() => {
  switch (props.color) {
    case 'blue': return 'bg-blue-600/80'
    case 'green': return 'bg-green-600/80'
    case 'purple': return 'bg-purple-600/80'
    default: return 'bg-orange-600/80'
  }
})
</script>

<style scoped>
</style>

