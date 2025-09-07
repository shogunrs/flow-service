<template>
  <Teleport to="body">
    <transition-group name="toast-list" tag="div" :class="containerClass">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="pointer-events-auto w-[280px] rounded-lg border px-3 py-2 shadow backdrop-blur-[2px]"
        :class="toastBg(t.type)"
        role="status"
        aria-live="polite"
      >
        <div class="flex items-start gap-2">
          <i :class="iconClass(t.type)" class="mt-0.5"></i>
          <div class="min-w-0 flex-1">
            <div v-if="t.title" class="text-[12px] font-semibold text-slate-100 truncate">{{ t.title }}</div>
            <div class="text-[12px] text-slate-200 leading-snug break-words">{{ t.message }}</div>
          </div>
          <button class="text-slate-300 hover:text-white ml-1" @click="remove(t.id)">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>
    </transition-group>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useToast } from '~/composables/useToast'

const props = defineProps({
  position: { type: String, default: 'bottom-right' } // 'bottom-right' | 'top-right' | 'bottom-left' | 'top-left'
})

const { toasts, remove } = useToast()

const containerClass = computed(() => {
  const base = 'fixed z-[9999] pointer-events-none flex flex-col gap-2 p-4'
  switch (props.position) {
    case 'top-right': return base + ' top-0 right-0 items-end'
    case 'top-left': return base + ' top-0 left-0 items-start'
    case 'bottom-left': return base + ' bottom-0 left-0 items-start'
    default: return base + ' bottom-0 right-0 items-end'
  }
})

function toastBg(type) {
  const semi = 'bg-slate-900/80 border-slate-700/60'
  switch (type) {
    case 'success': return semi + ' text-green-200'
    case 'warning': return semi + ' text-amber-200'
    case 'error': return semi + ' text-red-200'
    default: return semi + ' text-slate-200'
  }
}
function iconClass(type) {
  switch (type) {
    case 'success': return 'fa-solid fa-circle-check text-green-400'
    case 'warning': return 'fa-solid fa-triangle-exclamation text-amber-400'
    case 'error': return 'fa-solid fa-circle-exclamation text-red-400'
    default: return 'fa-regular fa-circle-info text-slate-300'
  }
}
</script>

<style scoped>
.toast-list-enter-from,
.toast-list-leave-to { opacity: 0; transform: translateY(6px) scale(0.98); }
.toast-list-enter-active,
.toast-list-leave-active { transition: all 160ms ease; }
</style>
