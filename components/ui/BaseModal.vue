<template>
  <Teleport to="body">
    <transition name="fade">
      <div v-if="modelValue" class="fixed inset-0 flex items-center justify-center" :style="{ zIndex: zIndex }">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-[2px]" @click="onBackdrop"></div>
        <div
          class="relative z-10 bg-slate-800/90 text-white overflow-hidden flex flex-col border border-slate-700/60 shadow-2xl backdrop-blur-md"
          :class="containerClass"
        >
          <div class="flex items-center justify-between px-3 py-2 border-b border-slate-700/60 bg-gradient-to-r from-slate-900/60 to-slate-800/40">
            <h3 class="text-[13px] font-semibold tracking-wide">{{ title }}</h3>
            <button class="text-slate-300 hover:text-white p-1 rounded-lg hover:bg-white/5 transition-colors" @click="close" aria-label="Fechar">
              <i class="fa-solid fa-xmark text-sm"></i>
            </button>
          </div>
          <div class="p-3 overflow-y-auto" :class="bodyClass">
            <slot />
          </div>
          <div v-if="$slots.footer" class="border-t border-slate-700/60 p-2 bg-slate-900/30">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
  
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  size: { type: String, default: 'md' }, // xs|sm|md|lg|xl
  closeOnBackdrop: { type: Boolean, default: true },
  bodyClass: { type: String, default: '' },
  zIndex: { type: Number, default: 50 }
})

const emit = defineEmits(['update:modelValue'])

const containerClass = computed(() => {
  switch (props.size) {
    case 'xs': return 'w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[80vh] sm:rounded-2xl sm:max-w-xs'
    case 'sm': return 'w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[80vh] sm:rounded-2xl sm:max-w-sm'
    case 'lg': return 'w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[85vh] sm:rounded-2xl sm:max-w-3xl'
    case 'xl': return 'w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[85vh] sm:rounded-2xl sm:max-w-5xl'
    default: return 'w-full h-full rounded-none sm:w-auto sm:h-auto sm:max-h-[85vh] sm:rounded-2xl sm:max-w-xl'
  }
})

function close() { emit('update:modelValue', false) }
function onBackdrop() { if (props.closeOnBackdrop) close() }
</script>

<style scoped>
</style>
