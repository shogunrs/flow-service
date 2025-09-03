<template>
  <div class="relative">
    <!-- Arrow -->
    <div class="absolute -left-2 top-1/2 -translate-y-1/2 w-2 h-2 rotate-45 bg-slate-800 border-l border-t border-slate-700/60"></div>

    <div class="min-w-[14rem] max-w-[18rem] bg-slate-800/95 text-slate-100 rounded-xl border border-slate-700/60 shadow-2xl overflow-hidden">
      <div v-if="$slots.header" class="px-3 py-2 text-[12px] text-slate-300 border-b border-slate-700/60">
        <slot name="header" />
      </div>
      <ul class="py-1">
        <li v-for="(item, i) in items" :key="item.key || i">
          <component
            :is="item.to ? 'NuxtLink' : item.href ? 'a' : 'button'"
            :to="item.to"
            :href="item.href"
            class="w-full text-left px-3 py-2 text-[13px] hover:bg-slate-700/60 flex items-center gap-2"
            @click="$emit('select', item)"
          >
            <i v-if="item.icon" :class="[item.icon, 'text-slate-300']"></i>
            <span class="truncate">{{ item.label }}</span>
          </component>
        </li>
      </ul>
      <div v-if="$slots.footer" class="px-3 py-2 border-t border-slate-700/60">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ items: { type: Array, default: () => [] } })
defineEmits(['select'])
</script>

<style scoped>
</style>

