<template>
  <div class="ais-wrapper" :class="wrapperClasses">
    <!-- Left search icon -->
    <i class="fa-solid fa-magnifying-glass ais-left-icon" aria-hidden="true"></i>

    <!-- Input -->
    <input
      ref="inputRef"
      v-model="localValue"
      :placeholder="placeholder"
      :aria-label="ariaLabel || placeholder"
      :disabled="disabled"
      class="ais-input"
      :class="sizeClasses.input"
      @keydown.enter.prevent="emit('search', localValue)"
      @focus="emit('focus')"
      @blur="emit('blur')"
    />

    <!-- AI action chip -->
    <button
      v-if="showAi"
      type="button"
      class="ais-ai"
      :class="[sizeClasses.ai, { 'ais-ai--active': isActive }]"
      @click="onAiClick"
      :disabled="disabled"
    >
      <i class="fa-solid fa-robot" aria-hidden="true"></i>
      <span class="ais-ai-label">{{ aiLabel }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface Props {
  modelValue?: string
  placeholder?: string
  aiLabel?: string
  showAi?: boolean
  disabled?: boolean
  size?: 'sm' | 'md'
  ariaLabel?: string
  persistOnClick?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: 'Pesquisar',
  aiLabel: 'IA',
  showAi: true,
  disabled: false,
  size: 'md',
  ariaLabel: '',
  persistOnClick: true
})

const emit = defineEmits<{
  'update:modelValue': [val: string]
  search: [val: string]
  ai: [val: string]
  focus: []
  blur: []
}>()

const inputRef = ref<HTMLInputElement | null>(null)
const localValue = ref(props.modelValue)
const aiOn = ref(false)

watch(
  () => props.modelValue,
  (v) => {
    if (v !== localValue.value) localValue.value = v || ''
  }
)

watch(localValue, (v) => emit('update:modelValue', v))

const sizeClasses = computed(() => {
  return props.size === 'sm'
    ? { input: 'h-8 text-[12px] pl-8 pr-16', ai: 'h-5 text-[10px] px-1.5 gap-1' }
    : { input: 'h-9 text-sm pl-9 pr-20', ai: 'h-6 text-[11px] px-2 gap-1.5' }
})

const wrapperClasses = computed(() => [props.disabled ? 'opacity-60 cursor-not-allowed' : ''])

const isActive = computed(() => aiOn.value)

function onAiClick() {
  if (props.persistOnClick) aiOn.value = !aiOn.value
  emit('ai', localValue.value)
}

defineExpose({
  focus: () => inputRef.value?.focus(),
  setActive: (v: boolean) => (aiOn.value = !!v),
  toggleActive: () => (aiOn.value = !aiOn.value)
})
</script>

<style scoped>
.ais-wrapper {
  @apply relative w-full flex items-center rounded-md border bg-slate-800/80 border-slate-700 text-slate-200;
  @apply transition shadow-sm focus-within:ring-[1px] focus-within:ring-orange-500/60 focus-within:border-orange-400/50;
}
.ais-left-icon {
  @apply absolute left-2 text-slate-400 text-xs;
}
.ais-input {
  @apply w-full bg-transparent outline-none placeholder-slate-400;
}
.ais-ai {
  @apply absolute right-1 inline-flex items-center rounded-md border border-slate-600/60 bg-slate-700/60 text-slate-200;
  @apply transition-colors disabled:opacity-70 disabled:cursor-not-allowed;
  position: relative;
  overflow: hidden;
}
.ais-ai-label {
  @apply leading-none;
}

/* Multicolor hover effect */
.ais-ai::before {
  content: "";
  position: absolute;
  inset: -2px;
  border-radius: inherit;
  background: conic-gradient(
    from 0deg,
    #ff6b6b,
    #f7b733,
    #4cd137,
    #00a8ff,
    #9c88ff,
    #ff6b6b
  );
  filter: blur(8px);
  opacity: 0;
  transition: opacity 200ms ease;
  animation: ais-spin 3.5s linear infinite;
  z-index: 0;
}

.ais-ai:hover::before,
.ais-ai:focus-visible::before {
  opacity: 0.85;
}

.ais-ai--active::before { opacity: 0.85; }

/* Elevate content above the effect */
.ais-ai > * {
  position: relative;
  z-index: 1;
}

/* Also animate the background on hover for a subtle rainbow sweep */
.ais-ai:hover,
.ais-ai:focus-visible {
  color: #fff;
  border-color: transparent;
  background-image: linear-gradient(90deg, #1f2937, #1f2937), linear-gradient(90deg, #ff6b6b, #f7b733, #4cd137, #00a8ff, #9c88ff, #ff6b6b);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  box-shadow: 0 0 0 1px rgba(249, 115, 22, 0.35), 0 4px 12px rgba(0,0,0,.35);
  animation: ais-gradient 4s ease infinite;
}

.ais-ai--active {
  color: #fff;
  border-color: transparent;
  background-image: linear-gradient(90deg, #1f2937, #1f2937), linear-gradient(90deg, #ff6b6b, #f7b733, #4cd137, #00a8ff, #9c88ff, #ff6b6b);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  box-shadow: 0 0 0 1px rgba(249, 115, 22, 0.35), 0 4px 12px rgba(0,0,0,.35);
  animation: ais-gradient 4s ease infinite;
}

@keyframes ais-spin {
  to { transform: rotate(1turn); }
}

@keyframes ais-gradient {
  0% { background-position: 0% 50%, 0% 50%; }
  50% { background-position: 0% 50%, 100% 50%; }
  100% { background-position: 0% 50%, 0% 50%; }
}
</style>
