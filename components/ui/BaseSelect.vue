<template>
  <div class="select-wrapper" :class="wrapperClasses">
    <label v-if="label" :for="selectId" class="select-label">{{ label }}</label>
    <div class="select-container" :class="containerClasses">
      <select
        :id="selectId"
        class="select-field"
        :class="fieldClasses"
        :disabled="disabled"
        :required="required"
        v-model="model"
      >
        <option v-if="placeholder" value="">{{ placeholder }}</option>
        <option v-for="opt in options" :key="String(opt.value)" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>
    <p v-if="errorMessage" class="select-error">{{ errorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, useId } from 'vue'

interface Option { label: string; value: string | number }
interface Props {
  modelValue?: string | number
  options: Option[]
  label?: string
  placeholder?: string
  size?: 'xs' | 'sm' | 'md'
  disabled?: boolean
  required?: boolean
  errorMessage?: string
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  placeholder: 'Selecione',
  size: 'xs',
  disabled: false,
  required: false,
  errorMessage: ''
})

const emit = defineEmits<{ 'update:modelValue': [val: string | number] }>()

const selectId = useId()

const model = computed({
  get: () => props.modelValue ?? '',
  set: (v: string | number) => emit('update:modelValue', v)
})

const wrapperClasses = computed(() => [])
const containerClasses = computed(() => [`select-container--${props.size}`])
const fieldClasses = computed(() => [`select-field--${props.size}`])
</script>

<style scoped>
.select-label { @apply text-[10px] text-slate-300; }
.select-container { @apply relative; }
.select-container--xs { @apply h-auto; }
.select-container--sm { @apply h-8; }
.select-container--md { @apply h-10; }
.select-field {
  @apply w-full bg-slate-700/70 border border-slate-600/60 rounded-sm outline-none transition appearance-none cursor-pointer;
  -webkit-appearance: none;
  color: #E5E7EB !important;
  -webkit-text-fill-color: #E5E7EB !important;
}
.select-field--xs {
  height: 1.75rem; /* h-7 */
  line-height: 1.75rem; /* align text vertically */
  @apply px-2 pr-6 text-[12px] bg-clip-padding;
  padding-top: 0; padding-bottom: 0;
  @apply focus:ring-[0.5px] focus:ring-indigo-500/60 focus:border-indigo-400/50;
}
.select-field--sm { @apply h-8 px-2 text-sm focus:ring-1 focus:ring-indigo-500/60; }
.select-field--md { @apply h-10 px-3 text-base focus:ring-1 focus:ring-indigo-500/60; }
.select-error { @apply text-[11px] text-red-400 mt-0.5; }
</style>
