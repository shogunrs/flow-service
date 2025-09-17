<template>
  <div class="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
    <div class="text-[12px] text-slate-400 mb-2">Ações Rápidas</div>
    <div class="grid grid-cols-2 gap-2">
      <button
        v-for="(a, i) in actions"
        :key="a.key || i"
        class="rounded-lg bg-slate-800 hover:bg-slate-700/70 border border-slate-700/60 text-slate-100 text-[12px] px-3 py-2 text-left flex items-center gap-2"
        @click="$emit('action', a)"
      >
        <i :class="[a.icon || 'fa-solid fa-bolt', 'text-indigo-400']"></i>
        <span class="truncate">{{ a.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({ actions: { type: Array, default: () => [] } })
defineEmits(['action'])
</script>

<style scoped>
</style>

