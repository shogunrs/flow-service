<template>
  <div class="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
    <div class="text-[12px] text-slate-400 mb-2">Atividades Recentes</div>
    <ul class="space-y-3">
      <li v-for="(a, i) in items" :key="a.id || i" class="flex items-start gap-2">
        <div class="w-8 h-8 rounded-lg flex items-center justify-center bg-slate-700/60 text-slate-200 shrink-0">
          <i :class="a.icon || 'fa-regular fa-clock'"></i>
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-[13px] text-white truncate">{{ a.title }}</div>
          <div class="text-[11px] text-slate-400">{{ a.time }}</div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
defineProps({ items: { type: Array, default: () => [] } })
</script>

<style scoped>
</style>

